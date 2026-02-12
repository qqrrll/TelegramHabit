package org.example.telegramhabit.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.FriendInviteResponse;
import org.example.telegramhabit.dto.FriendResponse;
import org.example.telegramhabit.entity.FriendInviteEntity;
import org.example.telegramhabit.entity.FriendshipEntity;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.repository.FriendInviteRepository;
import org.example.telegramhabit.repository.FriendshipRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Friend graph and invite-link workflow. */
@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final FriendInviteRepository friendInviteRepository;

    @Value("${app.friends.invite-base-url:http://localhost:5173/friends}")
    private String inviteBaseUrl;

    @Value("${app.telegram.bot-username:}")
    private String botUsername;

    @Value("${app.telegram.miniapp-short-name:}")
    private String miniAppShortName;

    @Transactional(readOnly = true)
    public List<FriendResponse> listFriends(UserEntity user) {
        return friendshipRepository.findByUser(user).stream()
                .map(edge -> {
                    UserEntity friend = edge.getFriend();
                    return new FriendResponse(
                            friend.getId(),
                            friend.getUsername(),
                            friend.getFirstName(),
                            friend.getLastName(),
                            friend.getPhotoUrl()
                    );
                })
                .toList();
    }

    @Transactional
    public FriendInviteResponse createInvite(UserEntity user) {
        FriendInviteEntity invite = new FriendInviteEntity();
        invite.setId(UUID.randomUUID());
        invite.setCode(UUID.randomUUID().toString().replace("-", ""));
        invite.setInviter(user);
        invite.setCreatedAt(LocalDateTime.now());
        invite.setExpiresAt(LocalDateTime.now().plusDays(7));
        friendInviteRepository.save(invite);
        String url = buildInviteUrl(invite.getCode());
        return new FriendInviteResponse(invite.getCode(), url, invite.getExpiresAt().toString());
    }

    @Transactional
    public FriendResponse acceptInvite(UserEntity user, String code) {
        FriendInviteEntity invite = friendInviteRepository.findByCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Invite not found"));

        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invite expired");
        }
        UserEntity inviter = invite.getInviter();
        if (inviter.getId().equals(user.getId())) {
            throw new IllegalArgumentException("Cannot accept your own invite");
        }

        createEdgeIfMissing(inviter, user);
        createEdgeIfMissing(user, inviter);

        if (invite.getUsedAt() == null) {
            invite.setUsedAt(LocalDateTime.now());
        }
        friendInviteRepository.save(invite);

        return toFriendResponse(inviter);
    }

    @Transactional(readOnly = true)
    public List<UserEntity> friendsOf(UserEntity user) {
        return friendshipRepository.findByUser(user).stream()
                .map(FriendshipEntity::getFriend)
                .toList();
    }

    @Transactional
    public void removeFriend(UserEntity user, UUID friendId) {
        UserEntity friend = requireFriend(user, friendId);

        friendshipRepository.deleteByUserAndFriend(user, friend);
        friendshipRepository.deleteByUserAndFriend(friend, user);
    }

    @Transactional(readOnly = true)
    public UserEntity requireFriend(UserEntity user, UUID friendId) {
        return friendsOf(user).stream()
                .filter(candidate -> candidate.getId().equals(friendId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Friend not found"));
    }

    @Transactional(readOnly = true)
    public FriendResponse friendProfile(UserEntity user, UUID friendId) {
        return toFriendResponse(requireFriend(user, friendId));
    }

    private String buildInviteUrl(String code) {
        String encoded = URLEncoder.encode("friend_" + code, StandardCharsets.UTF_8);
        if (!botUsername.isBlank() && !miniAppShortName.isBlank()) {
            return "https://t.me/" + botUsername + "/" + miniAppShortName + "?startapp=" + encoded;
        }
        return inviteBaseUrl + "?code=" + code;
    }

    private void createEdgeIfMissing(UserEntity user, UserEntity friend) {
        if (friendshipRepository.findByUserAndFriend(user, friend).isPresent()) {
            return;
        }
        FriendshipEntity edge = new FriendshipEntity();
        edge.setId(UUID.randomUUID());
        edge.setUser(user);
        edge.setFriend(friend);
        edge.setCreatedAt(LocalDateTime.now());
        friendshipRepository.save(edge);
    }

    private FriendResponse toFriendResponse(UserEntity friend) {
        return new FriendResponse(
                friend.getId(),
                friend.getUsername(),
                friend.getFirstName(),
                friend.getLastName(),
                friend.getPhotoUrl()
        );
    }
}

package org.example.telegramhabit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.AcceptFriendInviteRequest;
import org.example.telegramhabit.dto.FriendInviteResponse;
import org.example.telegramhabit.dto.FriendResponse;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.security.SecurityUtils;
import org.example.telegramhabit.service.FriendService;
import org.example.telegramhabit.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Friend management endpoints for mini-app social mode. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;
    private final UserService userService;

    @GetMapping
    public List<FriendResponse> list() {
        return friendService.listFriends(currentUser());
    }

    @PostMapping("/invite")
    public FriendInviteResponse createInvite() {
        return friendService.createInvite(currentUser());
    }

    @PostMapping("/accept")
    public FriendResponse acceptInvite(@Valid @RequestBody AcceptFriendInviteRequest request) {
        return friendService.acceptInvite(currentUser(), request.code());
    }

    @DeleteMapping("/{friendId}")
    public void remove(@PathVariable UUID friendId) {
        friendService.removeFriend(currentUser(), friendId);
    }

    private UserEntity currentUser() {
        return userService.requireById(SecurityUtils.currentUserId());
    }
}

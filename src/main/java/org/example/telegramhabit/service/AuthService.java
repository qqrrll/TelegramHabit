package org.example.telegramhabit.service;

import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.AuthResponse;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.repository.UserRepository;
import org.example.telegramhabit.security.JwtService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/** Handles Telegram sign-in and JWT issuance. */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final TelegramInitDataValidator initDataValidator;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse authenticateTelegram(String initData) {
        TelegramInitDataValidator.TelegramUserData data = initDataValidator.validateAndExtract(initData);
        return upsertAndIssueToken(
                data.telegramId(),
                data.firstName(),
                data.lastName(),
                data.username(),
                data.photoUrl()
        );
    }

    @Transactional
    public AuthResponse authenticateDev(Long telegramId, String firstName, String username) {
        return upsertAndIssueToken(telegramId, firstName, null, username, null);
    }

    private AuthResponse upsertAndIssueToken(
            Long telegramId,
            String firstName,
            String lastName,
            String username,
            String photoUrl
    ) {
        UserEntity user = userRepository.findByTelegramId(telegramId)
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity();
                    newUser.setId(UUID.randomUUID());
                    newUser.setTelegramId(telegramId);
                    newUser.setCreatedAt(LocalDateTime.now());
                    return newUser;
                });

        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhotoUrl(photoUrl);

        userRepository.save(user);
        String token = jwtService.generate(user.getId());
        return new AuthResponse(token, user.getId(), user.getFirstName(), user.getUsername());
    }
}

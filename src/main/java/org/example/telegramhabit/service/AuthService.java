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

@Service
@RequiredArgsConstructor
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class AuthService {

    private final TelegramInitDataValidator initDataValidator;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Transactional
    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public AuthResponse authenticateTelegram(String initData) {
        TelegramInitDataValidator.TelegramUserData data = initDataValidator.validateAndExtract(initData);
        return upsertAndIssueToken(
                data.telegramId(),
                data.firstName(),
                data.lastName(),
                data.username(),
                data.photoUrl(),
                data.language()
        );
    }

    @Transactional
    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public AuthResponse authenticateDev(Long telegramId, String firstName, String username) {
        return upsertAndIssueToken(telegramId, firstName, null, username, null, "en");
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private AuthResponse upsertAndIssueToken(
            Long telegramId,
            String firstName,
            String lastName,
            String username,
            String photoUrl,
            String language
    ) {
        UserEntity user = userRepository.findByTelegramId(telegramId)
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity();
                    newUser.setId(UUID.randomUUID());
                    newUser.setTelegramId(telegramId);
                    newUser.setCreatedAt(LocalDateTime.now());
                    newUser.setLanguage("en");
                    return newUser;
                });

        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        if (canOverridePhoto(user, photoUrl)) {
            user.setPhotoUrl(photoUrl);
        }
        user.setLanguage(language == null || language.isBlank() ? "en" : language);

        userRepository.save(user);
        String token = jwtService.generate(user.getId());
        return new AuthResponse(token, user.getId(), user.getFirstName(), user.getUsername());
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private boolean canOverridePhoto(UserEntity user, String incomingPhotoUrl) {
        if (incomingPhotoUrl == null || incomingPhotoUrl.isBlank()) {
            return false;
        }
        String current = user.getPhotoUrl();
        return current == null || current.isBlank() || !current.startsWith("/uploads/");
    }
}

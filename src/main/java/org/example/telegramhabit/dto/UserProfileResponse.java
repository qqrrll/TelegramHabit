package org.example.telegramhabit.dto;

import java.util.UUID;

// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public record UserProfileResponse(
        UUID id,
        Long telegramId,
        String username,
        String firstName,
        String lastName,
        String photoUrl,
        String language
) {
}

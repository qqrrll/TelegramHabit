package org.example.telegramhabit.dto;

import java.util.UUID;

// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public record FriendResponse(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String photoUrl
) {
}

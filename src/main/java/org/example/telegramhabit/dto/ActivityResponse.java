package org.example.telegramhabit.dto;

import org.example.telegramhabit.entity.ActivityType;

import java.time.LocalDateTime;
import java.util.UUID;

// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public record ActivityResponse(
        UUID id,
        UUID habitId,
        UUID userId,
        String actorName,
        String actorPhotoUrl,
        boolean ownEvent,
        ActivityType type,
        String message,
        LocalDateTime createdAt,
        long createdAtEpochMs
) {
}

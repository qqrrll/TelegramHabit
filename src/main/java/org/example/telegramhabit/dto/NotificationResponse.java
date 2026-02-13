package org.example.telegramhabit.dto;

import org.example.telegramhabit.entity.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public record NotificationResponse(
        UUID id,
        NotificationType type,
        String message,
        boolean read,
        UUID activityId,
        UUID actorUserId,
        String actorName,
        String actorPhotoUrl,
        LocalDateTime createdAt,
        long createdAtEpochMs
) {
}

package org.example.telegramhabit.dto;

import org.example.telegramhabit.entity.ActivityType;

import java.time.LocalDateTime;
import java.util.UUID;

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

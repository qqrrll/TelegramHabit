package org.example.telegramhabit.dto;

import org.example.telegramhabit.entity.ActivityType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ActivityResponse(
        UUID id,
        UUID habitId,
        ActivityType type,
        String message,
        LocalDateTime createdAt
) {
}

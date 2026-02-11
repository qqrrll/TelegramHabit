package org.example.telegramhabit.dto;

import org.example.telegramhabit.entity.HabitType;

import java.time.LocalDateTime;
import java.util.UUID;

public record HabitResponse(
        UUID id,
        String title,
        HabitType type,
        Integer timesPerWeek,
        String color,
        String icon,
        boolean archived,
        int currentStreak,
        int bestStreak,
        LocalDateTime createdAt
) {
}

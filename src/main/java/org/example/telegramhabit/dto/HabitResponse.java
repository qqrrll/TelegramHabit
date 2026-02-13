package org.example.telegramhabit.dto;

import org.example.telegramhabit.entity.HabitType;

import java.time.LocalDateTime;
import java.util.UUID;

// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public record HabitResponse(
        UUID id,
        String title,
        HabitType type,
        Integer timesPerWeek,
        String color,
        String icon,
        String imageUrl,
        boolean archived,
        int currentStreak,
        int bestStreak,
        LocalDateTime createdAt
) {
}

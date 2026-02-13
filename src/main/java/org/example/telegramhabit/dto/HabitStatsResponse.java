package org.example.telegramhabit.dto;

// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public record HabitStatsResponse(
        int completedThisWeek,
        int targetThisWeek,
        int completedThisMonth,
        int targetThisMonth,
        int completionPercentWeek,
        int completionPercentMonth,
        int currentStreak,
        int bestStreak
) {
}

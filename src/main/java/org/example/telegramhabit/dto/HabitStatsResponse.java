package org.example.telegramhabit.dto;

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

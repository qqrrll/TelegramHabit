package org.example.telegramhabit.dto;

public record HabitReactionSummaryResponse(
        String emoji,
        long count,
        boolean mine
) {
}

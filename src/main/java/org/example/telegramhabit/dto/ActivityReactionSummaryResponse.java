package org.example.telegramhabit.dto;

// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public record ActivityReactionSummaryResponse(
        String emoji,
        long count,
        boolean mine
) {
}

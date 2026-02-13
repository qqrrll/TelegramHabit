package org.example.telegramhabit.dto;

import jakarta.validation.constraints.NotBlank;

// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public record HabitReactionRequest(
        @NotBlank String emoji
) {
}

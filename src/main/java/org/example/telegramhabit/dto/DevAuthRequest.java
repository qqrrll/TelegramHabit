package org.example.telegramhabit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public record DevAuthRequest(
        @NotNull Long telegramId,
        @NotBlank String firstName,
        String username
) {
}

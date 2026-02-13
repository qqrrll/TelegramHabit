package org.example.telegramhabit.dto;

import jakarta.validation.constraints.NotBlank;

public record HabitReactionRequest(
        @NotBlank String emoji
) {
}

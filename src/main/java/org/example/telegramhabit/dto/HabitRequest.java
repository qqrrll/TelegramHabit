package org.example.telegramhabit.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.telegramhabit.entity.HabitType;

public record HabitRequest(
        @NotBlank String title,
        @NotNull HabitType type,
        @Min(1) @Max(7) Integer timesPerWeek,
        @NotBlank String color,
        @NotBlank String icon,
        boolean archived
) {
}

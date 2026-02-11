package org.example.telegramhabit.dto;

import jakarta.validation.constraints.NotBlank;

public record TelegramAuthRequest(@NotBlank String initData) {
}

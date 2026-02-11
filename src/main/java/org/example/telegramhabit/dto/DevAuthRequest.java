package org.example.telegramhabit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Local-only authentication payload for development without Telegram. */
public record DevAuthRequest(
        @NotNull Long telegramId,
        @NotBlank String firstName,
        String username
) {
}

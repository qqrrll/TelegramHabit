package org.example.telegramhabit.dto;

import jakarta.validation.constraints.NotBlank;

/** Language change request for profile settings. */
public record UpdateLanguageRequest(@NotBlank String language) {
}

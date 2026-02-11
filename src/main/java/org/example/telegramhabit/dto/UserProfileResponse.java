package org.example.telegramhabit.dto;

import java.util.UUID;

/** User profile payload for mini-app settings screen. */
public record UserProfileResponse(
        UUID id,
        Long telegramId,
        String username,
        String firstName,
        String lastName,
        String photoUrl,
        String language
) {
}

package org.example.telegramhabit.dto;

import java.util.UUID;

/** Minimal friend profile used in friends tab. */
public record FriendResponse(
        UUID id,
        String username,
        String firstName,
        String lastName,
        String photoUrl
) {
}

package org.example.telegramhabit.dto;

import java.util.UUID;

public record AuthResponse(String token, UUID userId, String firstName, String username) {
}

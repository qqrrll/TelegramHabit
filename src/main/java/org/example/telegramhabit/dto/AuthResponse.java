package org.example.telegramhabit.dto;

import java.util.UUID;

// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public record AuthResponse(String token, UUID userId, String firstName, String username) {
}

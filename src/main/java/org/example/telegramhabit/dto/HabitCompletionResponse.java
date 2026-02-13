package org.example.telegramhabit.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public record HabitCompletionResponse(UUID id, LocalDate date, boolean completed, LocalDateTime createdAt) {
}

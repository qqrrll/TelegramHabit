package org.example.telegramhabit.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record HabitCompletionResponse(UUID id, LocalDate date, boolean completed, LocalDateTime createdAt) {
}

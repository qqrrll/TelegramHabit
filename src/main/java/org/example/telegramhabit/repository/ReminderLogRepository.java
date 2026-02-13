package org.example.telegramhabit.repository;

import org.example.telegramhabit.entity.ReminderLogEntity;
import org.example.telegramhabit.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.UUID;

// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public interface ReminderLogRepository extends JpaRepository<ReminderLogEntity, UUID> {
    boolean existsByUserAndDate(UserEntity user, LocalDate date);
}

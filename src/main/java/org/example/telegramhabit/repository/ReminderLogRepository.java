package org.example.telegramhabit.repository;

import org.example.telegramhabit.entity.ReminderLogEntity;
import org.example.telegramhabit.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.UUID;

public interface ReminderLogRepository extends JpaRepository<ReminderLogEntity, UUID> {
    boolean existsByUserAndDate(UserEntity user, LocalDate date);
}

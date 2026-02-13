package org.example.telegramhabit.repository;

import org.example.telegramhabit.entity.HabitEntity;
import org.example.telegramhabit.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public interface HabitRepository extends JpaRepository<HabitEntity, UUID> {
    List<HabitEntity> findByUserOrderByCreatedAtDesc(UserEntity user);
    List<HabitEntity> findByUserAndArchivedFalseOrderByCreatedAtDesc(UserEntity user);

    Optional<HabitEntity> findByIdAndUser(UUID id, UserEntity user);
}

package org.example.telegramhabit.repository;

import org.example.telegramhabit.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByTelegramId(Long telegramId);
}

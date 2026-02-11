package org.example.telegramhabit.repository;

import org.example.telegramhabit.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/** JPA access for application users. */
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByTelegramId(Long telegramId);
}

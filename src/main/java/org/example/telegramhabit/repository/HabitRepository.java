package org.example.telegramhabit.repository;

import org.example.telegramhabit.entity.HabitEntity;
import org.example.telegramhabit.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** JPA access for habits scoped by owner. */
public interface HabitRepository extends JpaRepository<HabitEntity, UUID> {
    List<HabitEntity> findByUserOrderByCreatedAtDesc(UserEntity user);

    Optional<HabitEntity> findByIdAndUser(UUID id, UserEntity user);
}

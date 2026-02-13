package org.example.telegramhabit.repository;

import org.example.telegramhabit.entity.HabitCompletionEntity;
import org.example.telegramhabit.entity.HabitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** JPA access for habit completion marks. */
public interface HabitCompletionRepository extends JpaRepository<HabitCompletionEntity, UUID> {
    Optional<HabitCompletionEntity> findByHabitAndDate(HabitEntity habit, LocalDate date);

    List<HabitCompletionEntity> findByHabitAndCompletedTrueOrderByDateDesc(HabitEntity habit);

    List<HabitCompletionEntity> findByHabitAndDateBetweenAndCompletedTrue(HabitEntity habit, LocalDate from, LocalDate to);

    void deleteByHabit(HabitEntity habit);
}

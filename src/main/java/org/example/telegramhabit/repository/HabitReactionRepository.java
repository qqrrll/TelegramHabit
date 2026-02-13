package org.example.telegramhabit.repository;

import org.example.telegramhabit.entity.HabitEntity;
import org.example.telegramhabit.entity.HabitReactionEntity;
import org.example.telegramhabit.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public interface HabitReactionRepository extends JpaRepository<HabitReactionEntity, UUID> {

    Optional<HabitReactionEntity> findByHabitAndReactorAndEmoji(HabitEntity habit, UserEntity reactor, String emoji);

    List<HabitReactionEntity> findByHabitAndReactor(HabitEntity habit, UserEntity reactor);
    void deleteByHabit(HabitEntity habit);

    @Query("""
            select r.emoji as emoji, count(r) as count
            from HabitReactionEntity r
            where r.habit = :habit
            group by r.emoji
            """)
    List<ReactionCountProjection> countByHabit(@Param("habit") HabitEntity habit);

    // Что делает: описывает ключевой компонент backend-слоя приложения.
    // Как делает: объявляет структуру и контракт, который используют остальные части системы.
    interface ReactionCountProjection {
        String getEmoji();

        long getCount();
    }
}

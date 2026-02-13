package org.example.telegramhabit.repository;

import org.example.telegramhabit.entity.ActivityLogEntity;
import org.example.telegramhabit.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.example.telegramhabit.entity.HabitEntity;

// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public interface ActivityLogRepository extends JpaRepository<ActivityLogEntity, UUID> {
    List<ActivityLogEntity> findTop100ByUserOrderByCreatedAtDesc(UserEntity user);

    List<ActivityLogEntity> findTop100ByUserInOrderByCreatedAtDesc(Collection<UserEntity> users);

    void deleteByHabit(HabitEntity habit);
}

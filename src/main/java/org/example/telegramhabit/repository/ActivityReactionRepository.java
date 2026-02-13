package org.example.telegramhabit.repository;

import org.example.telegramhabit.entity.ActivityReactionEntity;
import org.example.telegramhabit.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public interface ActivityReactionRepository extends JpaRepository<ActivityReactionEntity, UUID> {

    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    Optional<ActivityReactionEntity> findByActivityIdAndReactorAndEmoji(UUID activityId, UserEntity reactor, String emoji);

    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    List<ActivityReactionEntity> findByActivityIdInAndReactor(Collection<UUID> activityIds, UserEntity reactor);

    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    @Query("""
            select r.activity.id as activityId, r.emoji as emoji, count(r) as count
            from ActivityReactionEntity r
            where r.activity.id in :activityIds
            group by r.activity.id, r.emoji
            """)
    List<ActivityReactionCountProjection> countByActivityIds(@Param("activityIds") Collection<UUID> activityIds);

    // Что делает: описывает ключевой компонент backend-слоя приложения.
    // Как делает: объявляет структуру и контракт, который используют остальные части системы.
    interface ActivityReactionCountProjection {
        UUID getActivityId();

        String getEmoji();

        long getCount();
    }
}

package org.example.telegramhabit.repository;

import org.example.telegramhabit.entity.NotificationEntity;
import org.example.telegramhabit.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    List<NotificationEntity> findTop100ByRecipientOrderByCreatedAtDesc(UserEntity recipient);

    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    Optional<NotificationEntity> findByIdAndRecipient(UUID id, UserEntity recipient);

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    long countByRecipientAndReadFalse(UserEntity recipient);
}

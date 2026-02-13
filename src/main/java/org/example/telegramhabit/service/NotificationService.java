package org.example.telegramhabit.service;

import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.NotificationResponse;
import org.example.telegramhabit.entity.ActivityLogEntity;
import org.example.telegramhabit.entity.NotificationEntity;
import org.example.telegramhabit.entity.NotificationType;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // Что делает: создаёт или сохраняет данные и возвращает результат операции.
    // Как делает: валидирует вход, заполняет поля, сохраняет в БД или хранилище и возвращает итог.
    @Transactional
    public void createReactionNotification(UserEntity recipient, UserEntity actor, ActivityLogEntity activity, String emoji) {
        if (recipient.getId().equals(actor.getId())) {
            return;
        }
        NotificationEntity notification = new NotificationEntity();
        notification.setId(UUID.randomUUID());
        notification.setRecipient(recipient);
        notification.setActor(actor);
        notification.setActivity(activity);
        notification.setType(NotificationType.REACTION);
        notification.setMessage(reactionMessage(recipient, actor, emoji));
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    @Transactional(readOnly = true)
    public List<NotificationResponse> list(UserEntity recipient) {
        return notificationRepository.findTop100ByRecipientOrderByCreatedAtDesc(recipient).stream()
                .map(this::toResponse)
                .toList();
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    @Transactional
    public void markAllRead(UserEntity recipient) {
        List<NotificationEntity> notifications = notificationRepository.findTop100ByRecipientOrderByCreatedAtDesc(recipient);
        for (NotificationEntity notification : notifications) {
            if (!notification.isRead()) {
                notification.setRead(true);
            }
        }
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    @Transactional
    public void markRead(UserEntity recipient, UUID notificationId) {
        notificationRepository.findByIdAndRecipient(notificationId, recipient)
                .ifPresent(notification -> notification.setRead(true));
    }

    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    @Transactional(readOnly = true)
    public long unreadCount(UserEntity recipient) {
        return notificationRepository.countByRecipientAndReadFalse(recipient);
    }

    // Что делает: преобразует или обновляет данные по правилам сервиса.
    // Как делает: применяет правила преобразования, затем сохраняет или возвращает обновлённые данные.
    private NotificationResponse toResponse(NotificationEntity notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getMessage(),
                notification.isRead(),
                notification.getActivity() != null ? notification.getActivity().getId() : null,
                notification.getActor().getId(),
                displayName(notification.getActor()),
                notification.getActor().getPhotoUrl(),
                notification.getCreatedAt(),
                notification.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        );
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private String reactionMessage(UserEntity recipient, UserEntity actor, String emoji) {
        if (recipient.getLanguage() != null && recipient.getLanguage().equalsIgnoreCase("ru")) {
            return displayName(actor) + " отреагировал(а) " + emoji + " на вашу активность";
        }
        return displayName(actor) + " reacted " + emoji + " to your activity";
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private String displayName(UserEntity user) {
        if (user.getFirstName() != null && !user.getFirstName().isBlank()) {
            return user.getFirstName();
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return "@" + user.getUsername();
        }
        return "User";
    }
}

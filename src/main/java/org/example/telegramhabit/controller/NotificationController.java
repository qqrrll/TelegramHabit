package org.example.telegramhabit.controller;

import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.NotificationResponse;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.security.SecurityUtils;
import org.example.telegramhabit.service.NotificationService;
import org.example.telegramhabit.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping
    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    public List<NotificationResponse> list() {
        return notificationService.list(currentUser());
    }

    @GetMapping("/unread-count")
    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    public Map<String, Long> unreadCount() {
        return Map.of("count", notificationService.unreadCount(currentUser()));
    }

    @PatchMapping("/read-all")
    // Что делает: преобразует или обновляет данные по правилам сервиса.
    // Как делает: применяет правила преобразования, затем сохраняет или возвращает обновлённые данные.
    public void readAll() {
        notificationService.markAllRead(currentUser());
    }

    @PatchMapping("/{notificationId}/read")
    // Что делает: преобразует или обновляет данные по правилам сервиса.
    // Как делает: применяет правила преобразования, затем сохраняет или возвращает обновлённые данные.
    public void readOne(@PathVariable UUID notificationId) {
        notificationService.markRead(currentUser(), notificationId);
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private UserEntity currentUser() {
        return userService.requireById(SecurityUtils.currentUserId());
    }
}

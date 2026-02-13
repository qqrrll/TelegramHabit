package org.example.telegramhabit.controller;

import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.ActivityResponse;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.security.SecurityUtils;
import org.example.telegramhabit.service.ActivityService;
import org.example.telegramhabit.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/activity")
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class ActivityController {

    private final ActivityService activityService;
    private final UserService userService;

    @GetMapping
    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    public List<ActivityResponse> list() {
        UserEntity user = userService.requireById(SecurityUtils.currentUserId());
        return activityService.list(user);
    }
}

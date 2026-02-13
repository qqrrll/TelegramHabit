package org.example.telegramhabit.service;

import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.ActivityResponse;
import org.example.telegramhabit.dto.ActivityReactionSummaryResponse;
import org.example.telegramhabit.entity.ActivityLogEntity;
import org.example.telegramhabit.entity.ActivityType;
import org.example.telegramhabit.entity.HabitEntity;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class ActivityService {

    private final ActivityLogRepository activityLogRepository;
    private final FriendService friendService;
    private final ActivityReactionService activityReactionService;

    @Transactional
    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public void log(UserEntity user, HabitEntity habit, ActivityType type, String message) {
        ActivityLogEntity log = new ActivityLogEntity();
        log.setId(UUID.randomUUID());
        log.setUser(user);
        log.setHabit(habit);
        log.setType(type);
        log.setMessage(message);
        log.setCreatedAt(LocalDateTime.now());
        activityLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    public List<ActivityResponse> list(UserEntity user) {
        List<UserEntity> feedUsers = new ArrayList<>();
        feedUsers.add(user);
        feedUsers.addAll(friendService.friendsOf(user));

        List<ActivityLogEntity> logs = activityLogRepository.findTop100ByUserInOrderByCreatedAtDesc(feedUsers);
        List<UUID> activityIds = logs.stream().map(ActivityLogEntity::getId).toList();
        Map<UUID, List<ActivityReactionSummaryResponse>> reactionsByActivity = activityReactionService.summary(activityIds, user);

        return logs.stream()
                .map(log -> new ActivityResponse(
                        log.getId(),
                        log.getHabit() != null ? log.getHabit().getId() : null,
                        log.getUser().getId(),
                        displayName(log.getUser()),
                        log.getUser().getPhotoUrl(),
                        log.getUser().getId().equals(user.getId()),
                        log.getType(),
                        log.getMessage(),
                        log.getCreatedAt(),
                        log.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                        reactionsByActivity.getOrDefault(log.getId(), Collections.emptyList())
                ))
                .toList();
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

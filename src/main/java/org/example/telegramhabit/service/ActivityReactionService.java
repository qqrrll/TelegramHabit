package org.example.telegramhabit.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.ActivityReactionSummaryResponse;
import org.example.telegramhabit.entity.ActivityLogEntity;
import org.example.telegramhabit.entity.ActivityReactionEntity;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.repository.ActivityLogRepository;
import org.example.telegramhabit.repository.ActivityReactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class ActivityReactionService {

    private static final Set<String> ALLOWED_EMOJI = Set.of("🔥", "💪", "👏", "❤️", "🎯", "🚀");

    private final ActivityLogRepository activityLogRepository;
    private final ActivityReactionRepository activityReactionRepository;
    private final FriendService friendService;
    private final NotificationService notificationService;

    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    @Transactional(readOnly = true)
    public List<ActivityReactionSummaryResponse> listForActivity(UserEntity currentUser, UUID activityId) {
        ActivityLogEntity activity = requireAccessibleActivity(currentUser, activityId);
        return summary(List.of(activity.getId()), currentUser).getOrDefault(activity.getId(), List.of());
    }

    // Что делает: преобразует или обновляет данные по правилам сервиса.
    // Как делает: применяет правила преобразования, затем сохраняет или возвращает обновлённые данные.
    @Transactional
    public List<ActivityReactionSummaryResponse> toggleForActivity(UserEntity currentUser, UUID activityId, String emoji) {
        ActivityLogEntity activity = requireAccessibleActivity(currentUser, activityId);
        String normalizedEmoji = normalizeEmoji(emoji);

        activityReactionRepository.findByActivityIdAndReactorAndEmoji(activityId, currentUser, normalizedEmoji)
                .ifPresentOrElse(
                        activityReactionRepository::delete,
                        () -> {
                            ActivityReactionEntity reaction = new ActivityReactionEntity();
                            reaction.setId(UUID.randomUUID());
                            reaction.setActivity(activity);
                            reaction.setReactor(currentUser);
                            reaction.setEmoji(normalizedEmoji);
                            reaction.setCreatedAt(LocalDateTime.now());
                            activityReactionRepository.save(reaction);
                            notificationService.createReactionNotification(activity.getUser(), currentUser, activity, normalizedEmoji);
                        }
                );

        return summary(List.of(activityId), currentUser).getOrDefault(activityId, List.of());
    }

    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    @Transactional(readOnly = true)
    public java.util.Map<UUID, List<ActivityReactionSummaryResponse>> summary(List<UUID> activityIds, UserEntity currentUser) {
        if (activityIds.isEmpty()) {
            return java.util.Map.of();
        }
        java.util.Map<UUID, Set<String>> mineByActivity = activityReactionRepository.findByActivityIdInAndReactor(activityIds, currentUser).stream()
                .collect(Collectors.groupingBy(
                        r -> r.getActivity().getId(),
                        Collectors.mapping(ActivityReactionEntity::getEmoji, Collectors.toSet())
                ));

        java.util.Map<UUID, List<ActivityReactionSummaryResponse>> grouped = activityReactionRepository.countByActivityIds(activityIds).stream()
                .collect(Collectors.groupingBy(
                        ActivityReactionRepository.ActivityReactionCountProjection::getActivityId,
                        Collectors.mapping(
                                item -> new ActivityReactionSummaryResponse(
                                        item.getEmoji(),
                                        item.getCount(),
                                        mineByActivity.getOrDefault(item.getActivityId(), Set.of()).contains(item.getEmoji())
                                ),
                                Collectors.toList()
                        )
                ));

        grouped.replaceAll((id, list) -> list.stream()
                .sorted(Comparator.comparingLong(ActivityReactionSummaryResponse::count).reversed()
                        .thenComparing(ActivityReactionSummaryResponse::emoji))
                .toList());
        return grouped;
    }

    // Что делает: проверяет входные данные и извлекает нужные значения.
    // Как делает: проводит проверки и возвращает значение, либо бросает исключение при ошибке.
    private ActivityLogEntity requireAccessibleActivity(UserEntity currentUser, UUID activityId) {
        ActivityLogEntity activity = activityLogRepository.findById(activityId)
                .orElseThrow(() -> new EntityNotFoundException("Activity not found"));
        if (activity.getUser().getId().equals(currentUser.getId())) {
            return activity;
        }
        friendService.requireFriend(currentUser, activity.getUser().getId());
        return activity;
    }

    // Что делает: преобразует или обновляет данные по правилам сервиса.
    // Как делает: применяет правила преобразования, затем сохраняет или возвращает обновлённые данные.
    private String normalizeEmoji(String emoji) {
        if (emoji == null) {
            throw new IllegalArgumentException("Emoji is required");
        }
        String normalized = emoji.trim();
        if (!ALLOWED_EMOJI.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported reaction");
        }
        return normalized;
    }
}

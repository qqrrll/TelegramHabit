package org.example.telegramhabit.service;

import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.HabitReactionSummaryResponse;
import org.example.telegramhabit.entity.HabitEntity;
import org.example.telegramhabit.entity.HabitReactionEntity;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.repository.HabitReactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class HabitReactionService {

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private static final Set<String> ALLOWED_EMOJI = Set.of("🔥", "💪", "👏", "❤️", "🎯", "🚀");

    private final HabitReactionRepository habitReactionRepository;
    private final FriendService friendService;
    private final HabitService habitService;

    @Transactional(readOnly = true)
    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    public List<HabitReactionSummaryResponse> listForFriendHabit(UserEntity currentUser, UUID friendId, UUID habitId) {
        UserEntity friend = friendService.requireFriend(currentUser, friendId);
        HabitEntity habit = habitService.requireOwnedHabit(friend, habitId);
        return summary(currentUser, habit);
    }

    @Transactional
    // Что делает: преобразует или обновляет данные по правилам сервиса.
    // Как делает: применяет правила преобразования, затем сохраняет или возвращает обновлённые данные.
    public List<HabitReactionSummaryResponse> toggleForFriendHabit(UserEntity currentUser, UUID friendId, UUID habitId, String emoji) {
        UserEntity friend = friendService.requireFriend(currentUser, friendId);
        HabitEntity habit = habitService.requireOwnedHabit(friend, habitId);
        String normalizedEmoji = normalizeEmoji(emoji);

        habitReactionRepository.findByHabitAndReactorAndEmoji(habit, currentUser, normalizedEmoji)
                .ifPresentOrElse(
                        habitReactionRepository::delete,
                        () -> {
                            HabitReactionEntity reaction = new HabitReactionEntity();
                            reaction.setId(UUID.randomUUID());
                            reaction.setHabit(habit);
                            reaction.setReactor(currentUser);
                            reaction.setEmoji(normalizedEmoji);
                            reaction.setCreatedAt(LocalDateTime.now());
                            habitReactionRepository.save(reaction);
                        }
                );

        return summary(currentUser, habit);
    }

    @Transactional
    // Что делает: удаляет данные по условиям метода с учётом связей.
    // Как делает: проверяет доступ и существование сущности, затем удаляет связанные и целевые записи.
    public List<HabitReactionSummaryResponse> removeForFriendHabit(UserEntity currentUser, UUID friendId, UUID habitId, String emoji) {
        UserEntity friend = friendService.requireFriend(currentUser, friendId);
        HabitEntity habit = habitService.requireOwnedHabit(friend, habitId);
        String normalizedEmoji = normalizeEmoji(emoji);

        habitReactionRepository.findByHabitAndReactorAndEmoji(habit, currentUser, normalizedEmoji)
                .ifPresent(habitReactionRepository::delete);

        return summary(currentUser, habit);
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private List<HabitReactionSummaryResponse> summary(UserEntity currentUser, HabitEntity habit) {
        Set<String> mine = habitReactionRepository.findByHabitAndReactor(habit, currentUser).stream()
                .map(HabitReactionEntity::getEmoji)
                .collect(java.util.stream.Collectors.toSet());

        return habitReactionRepository.countByHabit(habit).stream()
                .map(item -> new HabitReactionSummaryResponse(item.getEmoji(), item.getCount(), mine.contains(item.getEmoji())))
                .sorted(Comparator.comparingLong(HabitReactionSummaryResponse::count).reversed()
                        .thenComparing(HabitReactionSummaryResponse::emoji))
                .toList();
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

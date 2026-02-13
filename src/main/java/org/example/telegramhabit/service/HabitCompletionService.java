package org.example.telegramhabit.service;

import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.HabitCompletionResponse;
import org.example.telegramhabit.entity.ActivityType;
import org.example.telegramhabit.entity.HabitCompletionEntity;
import org.example.telegramhabit.entity.HabitEntity;
import org.example.telegramhabit.entity.HabitType;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.repository.HabitCompletionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class HabitCompletionService {

    private final HabitCompletionRepository completionRepository;
    private final HabitService habitService;
    private final ActivityService activityService;
    private final StreakService streakService;

    @Transactional
    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public HabitCompletionResponse complete(UserEntity user, UUID habitId) {
        HabitEntity habit = habitService.requireOwnedHabit(user, habitId);
        LocalDate today = LocalDate.now();
        HabitCompletionEntity completion = completionRepository.findByHabitAndDate(habit, today)
                .orElseGet(() -> {
                    HabitCompletionEntity newCompletion = new HabitCompletionEntity();
                    newCompletion.setId(UUID.randomUUID());
                    newCompletion.setHabit(habit);
                    newCompletion.setDate(today);
                    newCompletion.setCreatedAt(LocalDateTime.now());
                    return newCompletion;
                });

        if (completion.isCompleted()) {
            return toResponse(completion);
        }

        completion.setCompleted(true);
        completionRepository.save(completion);

        activityService.log(user, habit, ActivityType.COMPLETED, completedMessage(user, habit));
        emitStreakEvents(user, habit);
        return toResponse(completion);
    }

    @Transactional
    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public void uncomplete(UserEntity user, UUID habitId, LocalDate date) {
        HabitEntity habit = habitService.requireOwnedHabit(user, habitId);
        completionRepository.findByHabitAndDate(habit, date).ifPresent(completionRepository::delete);
    }

    @Transactional(readOnly = true)
    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    public List<HabitCompletionResponse> history(UserEntity user, UUID habitId) {
        HabitEntity habit = habitService.requireOwnedHabit(user, habitId);
        return completionRepository.findByHabitAndCompletedTrueOrderByDateDesc(habit).stream()
                .map(this::toResponse)
                .toList();
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private HabitCompletionResponse toResponse(HabitCompletionEntity completion) {
        return new HabitCompletionResponse(
                completion.getId(),
                completion.getDate(),
                completion.isCompleted(),
                completion.getCreatedAt()
        );
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private void emitStreakEvents(UserEntity user, HabitEntity habit) {
        int current = streakService.currentStreak(habit);
        int best = streakService.bestStreak(habit);
        if (habit.getType() == HabitType.DAILY && current > 0 && current % 7 == 0) {
            activityService.log(user, habit, ActivityType.STREAK, streakMessage(user, habit, current));
        }
        if (current == best && current > 1) {
            activityService.log(user, habit, ActivityType.RECORD, recordMessage(user, habit, best));
        }
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private String completedMessage(UserEntity user, HabitEntity habit) {
        if (isRu(user)) {
            return "Выполнена привычка: " + habit.getTitle();
        }
        return "Completed habit: " + habit.getTitle();
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private String streakMessage(UserEntity user, HabitEntity habit, int current) {
        if (isRu(user)) {
            return "Серия " + current + " дней для " + habit.getTitle();
        }
        return current + " days streak for " + habit.getTitle();
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private String recordMessage(UserEntity user, HabitEntity habit, int best) {
        if (isRu(user)) {
            return "Новый рекорд: " + best + " для " + habit.getTitle();
        }
        return "New record: " + best + " for " + habit.getTitle();
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private boolean isRu(UserEntity user) {
        return user.getLanguage() != null && user.getLanguage().equalsIgnoreCase("ru");
    }
}

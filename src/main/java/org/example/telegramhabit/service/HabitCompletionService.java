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

/** Completion/uncompletion operations and related activity events. */
@Service
@RequiredArgsConstructor
public class HabitCompletionService {

    private final HabitCompletionRepository completionRepository;
    private final HabitService habitService;
    private final ActivityService activityService;
    private final StreakService streakService;

    @Transactional
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

        completion.setCompleted(true);
        completionRepository.save(completion);

        activityService.log(user, habit, ActivityType.COMPLETED, "Completed habit: " + habit.getTitle());
        emitStreakEvents(user, habit);
        return toResponse(completion);
    }

    @Transactional
    public void uncomplete(UserEntity user, UUID habitId, LocalDate date) {
        HabitEntity habit = habitService.requireOwnedHabit(user, habitId);
        completionRepository.findByHabitAndDate(habit, date).ifPresent(completionRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<HabitCompletionResponse> history(UserEntity user, UUID habitId) {
        HabitEntity habit = habitService.requireOwnedHabit(user, habitId);
        return completionRepository.findByHabitAndCompletedTrueOrderByDateDesc(habit).stream()
                .map(this::toResponse)
                .toList();
    }

    private HabitCompletionResponse toResponse(HabitCompletionEntity completion) {
        return new HabitCompletionResponse(
                completion.getId(),
                completion.getDate(),
                completion.isCompleted(),
                completion.getCreatedAt()
        );
    }

    private void emitStreakEvents(UserEntity user, HabitEntity habit) {
        int current = streakService.currentStreak(habit);
        int best = streakService.bestStreak(habit);
        if (habit.getType() == HabitType.DAILY && current > 0 && current % 7 == 0) {
            activityService.log(user, habit, ActivityType.STREAK, current + " days streak for " + habit.getTitle());
        }
        if (current == best && current > 1) {
            activityService.log(user, habit, ActivityType.RECORD, "New record: " + best + " for " + habit.getTitle());
        }
    }
}

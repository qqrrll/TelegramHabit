package org.example.telegramhabit.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.HabitRequest;
import org.example.telegramhabit.dto.HabitResponse;
import org.example.telegramhabit.dto.HabitStatsResponse;
import org.example.telegramhabit.entity.HabitEntity;
import org.example.telegramhabit.entity.HabitType;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.repository.HabitCompletionRepository;
import org.example.telegramhabit.repository.HabitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

/** Core CRUD and stats logic for habits. */
@Service
@RequiredArgsConstructor
public class HabitService {

    private final HabitRepository habitRepository;
    private final HabitCompletionRepository completionRepository;
    private final StreakService streakService;

    @Transactional(readOnly = true)
    public List<HabitResponse> list(UserEntity user) {
        return habitRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HabitResponse> listByOwner(UserEntity owner) {
        return habitRepository.findByUserOrderByCreatedAtDesc(owner).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public HabitResponse create(UserEntity user, HabitRequest request) {
        validateRequest(request);
        HabitEntity habit = new HabitEntity();
        habit.setId(UUID.randomUUID());
        habit.setUser(user);
        habit.setTitle(request.title());
        habit.setType(request.type());
        habit.setTimesPerWeek(request.type() == HabitType.WEEKLY ? request.timesPerWeek() : null);
        habit.setColor(request.color());
        habit.setIcon(request.icon());
        habit.setArchived(request.archived());
        habit.setCreatedAt(LocalDateTime.now());
        return toResponse(habitRepository.save(habit));
    }

    @Transactional
    public HabitResponse update(UserEntity user, UUID habitId, HabitRequest request) {
        validateRequest(request);
        HabitEntity habit = requireOwnedHabit(user, habitId);
        habit.setTitle(request.title());
        habit.setType(request.type());
        habit.setTimesPerWeek(request.type() == HabitType.WEEKLY ? request.timesPerWeek() : null);
        habit.setColor(request.color());
        habit.setIcon(request.icon());
        habit.setArchived(request.archived());
        return toResponse(habitRepository.save(habit));
    }

    @Transactional
    public void delete(UserEntity user, UUID habitId) {
        HabitEntity habit = requireOwnedHabit(user, habitId);
        habitRepository.delete(habit);
    }

    @Transactional(readOnly = true)
    public HabitStatsResponse stats(UserEntity user, UUID habitId) {
        HabitEntity habit = requireOwnedHabit(user, habitId);
        return statsForHabit(habit);
    }

    @Transactional(readOnly = true)
    public HabitStatsResponse statsByOwner(UserEntity owner, UUID habitId) {
        HabitEntity habit = requireOwnedHabit(owner, habitId);
        return statsForHabit(habit);
    }

    private HabitStatsResponse statsForHabit(HabitEntity habit) {
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        YearMonth month = YearMonth.from(now);
        LocalDate monthStart = month.atDay(1);
        LocalDate monthEnd = month.atEndOfMonth();

        int completedWeek = completionRepository.findByHabitAndDateBetweenAndCompletedTrue(habit, weekStart, weekEnd).size();
        int completedMonth = completionRepository.findByHabitAndDateBetweenAndCompletedTrue(habit, monthStart, monthEnd).size();
        int targetWeek = habit.getType() == HabitType.DAILY ? 7 : habit.getTimesPerWeek();
        int targetMonth = habit.getType() == HabitType.DAILY ? month.lengthOfMonth() : habit.getTimesPerWeek() * 4;

        int currentStreak = streakService.currentStreak(habit);
        int bestStreak = streakService.bestStreak(habit);
        return new HabitStatsResponse(
                completedWeek,
                targetWeek,
                completedMonth,
                targetMonth,
                percent(completedWeek, targetWeek),
                percent(completedMonth, targetMonth),
                currentStreak,
                bestStreak
        );
    }

    public HabitEntity requireOwnedHabit(UserEntity user, UUID habitId) {
        return habitRepository.findByIdAndUser(habitId, user)
                .orElseThrow(() -> new EntityNotFoundException("Habit not found"));
    }

    private HabitResponse toResponse(HabitEntity habit) {
        return new HabitResponse(
                habit.getId(),
                habit.getTitle(),
                habit.getType(),
                habit.getTimesPerWeek(),
                habit.getColor(),
                habit.getIcon(),
                habit.isArchived(),
                streakService.currentStreak(habit),
                streakService.bestStreak(habit),
                habit.getCreatedAt()
        );
    }

    // Keep DTO consistency with DB checks for clearer API errors.
    private void validateRequest(HabitRequest request) {
        if (request.type() == HabitType.DAILY && request.timesPerWeek() != null) {
            throw new IllegalArgumentException("timesPerWeek must be null for DAILY habit");
        }
        if (request.type() == HabitType.WEEKLY && request.timesPerWeek() == null) {
            throw new IllegalArgumentException("timesPerWeek is required for WEEKLY habit");
        }
    }

    private int percent(int completed, int target) {
        if (target <= 0) {
            return 0;
        }
        return Math.min(100, (int) Math.round((completed * 100.0) / target));
    }
}

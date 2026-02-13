package org.example.telegramhabit.service;

import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.entity.HabitCompletionEntity;
import org.example.telegramhabit.entity.HabitEntity;
import org.example.telegramhabit.entity.HabitType;
import org.example.telegramhabit.repository.HabitCompletionRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class StreakService {

    private final HabitCompletionRepository completionRepository;

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public int currentStreak(HabitEntity habit) {
        if (habit.getType() == HabitType.DAILY) {
            return currentDailyStreak(habit);
        }
        return currentWeeklyStreak(habit);
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public int bestStreak(HabitEntity habit) {
        if (habit.getType() == HabitType.DAILY) {
            return bestDailyStreak(habit);
        }
        return bestWeeklyStreak(habit);
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private int currentDailyStreak(HabitEntity habit) {
        List<LocalDate> dates = completionRepository.findByHabitAndCompletedTrueOrderByDateDesc(habit).stream()
                .map(HabitCompletionEntity::getDate)
                .toList();
        return currentDailyFromDates(dates, LocalDate.now());
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private int bestDailyStreak(HabitEntity habit) {
        List<LocalDate> dates = completionRepository.findByHabitAndCompletedTrueOrderByDateDesc(habit).stream()
                .map(HabitCompletionEntity::getDate)
                .sorted()
                .toList();

        int best = 0;
        int current = 0;
        LocalDate prev = null;
        for (LocalDate date : dates) {
            if (prev == null || prev.plusDays(1).equals(date)) {
                current++;
            } else if (!prev.equals(date)) {
                current = 1;
            }
            best = Math.max(best, current);
            prev = date;
        }
        return best;
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private int currentWeeklyStreak(HabitEntity habit) {
        List<LocalDate> dates = completionRepository.findByHabitAndCompletedTrueOrderByDateDesc(habit).stream()
                .map(HabitCompletionEntity::getDate)
                .toList();
        if (dates.isEmpty()) {
            return 0;
        }

        Set<LocalDate> set = new HashSet<>(dates);
        LocalDate weekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int streak = 0;
        while (isWeekCompleted(set, weekStart, habit.getTimesPerWeek())) {
            streak++;
            weekStart = weekStart.minusWeeks(1);
        }
        return streak;
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private int bestWeeklyStreak(HabitEntity habit) {
        List<LocalDate> dates = completionRepository.findByHabitAndCompletedTrueOrderByDateDesc(habit).stream()
                .map(HabitCompletionEntity::getDate)
                .toList();
        if (dates.isEmpty()) {
            return 0;
        }

        Set<LocalDate> set = new HashSet<>(dates);
        LocalDate min = dates.stream().min(LocalDate::compareTo).orElse(LocalDate.now());
        LocalDate max = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate cursor = min.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        int best = 0;
        int current = 0;
        while (!cursor.isAfter(max)) {
            if (isWeekCompleted(set, cursor, habit.getTimesPerWeek())) {
                current++;
                best = Math.max(best, current);
            } else {
                current = 0;
            }
            cursor = cursor.plusWeeks(1);
        }
        return best;
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private boolean isWeekCompleted(Set<LocalDate> completionDates, LocalDate weekStart, Integer timesPerWeek) {
        if (timesPerWeek == null) {
            return false;
        }
        int count = 0;
        for (int i = 0; i < 7; i++) {
            if (completionDates.contains(weekStart.plusDays(i))) {
                count++;
            }
        }
        return count >= timesPerWeek;
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private int currentDailyFromDates(List<LocalDate> descDates, LocalDate today) {
        if (descDates.isEmpty()) {
            return 0;
        }

        int idx = 0;
        LocalDate expected = today;
        if (!descDates.get(0).equals(today)) {
            if (descDates.get(0).equals(today.minusDays(1))) {
                expected = today.minusDays(1);
            } else {
                return 0;
            }
        }

        int streak = 0;
        while (idx < descDates.size()) {
            LocalDate date = descDates.get(idx);
            if (date.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
                idx++;
            } else if (idx > 0 && date.equals(descDates.get(idx - 1))) {
                idx++;
            } else {
                break;
            }
        }
        return streak;
    }
}

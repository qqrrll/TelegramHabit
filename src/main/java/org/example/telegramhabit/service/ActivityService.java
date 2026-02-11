package org.example.telegramhabit.service;

import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.ActivityResponse;
import org.example.telegramhabit.entity.ActivityLogEntity;
import org.example.telegramhabit.entity.ActivityType;
import org.example.telegramhabit.entity.HabitEntity;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.repository.ActivityLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Writes and reads activity feed events. */
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityLogRepository activityLogRepository;

    @Transactional
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
    public List<ActivityResponse> list(UserEntity user) {
        return activityLogRepository.findTop100ByUserOrderByCreatedAtDesc(user).stream()
                .map(log -> new ActivityResponse(
                        log.getId(),
                        log.getHabit() != null ? log.getHabit().getId() : null,
                        log.getType(),
                        log.getMessage(),
                        log.getCreatedAt()
                ))
                .toList();
    }
}

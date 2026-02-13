package org.example.telegramhabit.service;

import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.entity.HabitCompletionEntity;
import org.example.telegramhabit.entity.HabitEntity;
import org.example.telegramhabit.entity.ReminderLogEntity;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.repository.HabitCompletionRepository;
import org.example.telegramhabit.repository.HabitRepository;
import org.example.telegramhabit.repository.ReminderLogRepository;
import org.example.telegramhabit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class TelegramReminderService {

    private final UserRepository userRepository;
    private final HabitRepository habitRepository;
    private final HabitCompletionRepository completionRepository;
    private final ReminderLogRepository reminderLogRepository;

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private final RestClient restClient = RestClient.create();

    @Value("${app.telegram.bot-token:change-me}")
    private String botToken;

    @Value("${app.telegram.reminders.enabled:false}")
    private boolean remindersEnabled;

    @Value("${app.telegram.reminders.hour-local:20}")
    private int reminderHourLocal;

    @Value("${app.telegram.reminders.zone-id:UTC}")
    private String reminderZoneId;

    @Scheduled(cron = "${app.telegram.reminders.cron:0 */30 * * * *}")
    @Transactional
    // Что делает: отправляет сообщение или запрос во внешний сервис и возвращает статус.
    // Как делает: формирует внешний HTTP-запрос, отправляет его и обрабатывает возможные ошибки.
    public void sendDailyReminders() {
        if (!remindersEnabled || botToken == null || botToken.isBlank() || "change-me".equals(botToken)) {
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(reminderZoneId));
        if (now.getHour() != reminderHourLocal) {
            return;
        }

        LocalDate today = now.toLocalDate();
        for (UserEntity user : userRepository.findAll()) {
            if (user.getTelegramId() == null || reminderLogRepository.existsByUserAndDate(user, today)) {
                continue;
            }

            List<HabitEntity> activeHabits = habitRepository.findByUserAndArchivedFalseOrderByCreatedAtDesc(user);
            if (activeHabits.isEmpty()) {
                continue;
            }

            long pendingCount = activeHabits.stream()
                    .filter(habit -> completionRepository.findByHabitAndDate(habit, today)
                            .map(HabitCompletionEntity::isCompleted)
                            .orElse(false) == false)
                    .count();
            if (pendingCount <= 0) {
                continue;
            }

            if (sendReminder(user.getTelegramId(), reminderText(user, pendingCount))) {
                ReminderLogEntity log = new ReminderLogEntity();
                log.setId(UUID.randomUUID());
                log.setUser(user);
                log.setDate(today);
                log.setSentAt(LocalDateTime.now());
                reminderLogRepository.save(log);
            }
        }
    }

    // Что делает: отправляет сообщение или запрос во внешний сервис и возвращает статус.
    // Как делает: формирует внешний HTTP-запрос, отправляет его и обрабатывает возможные ошибки.
    private boolean sendReminder(Long chatId, String text) {
        try {
            restClient.post()
                    .uri("https://api.telegram.org/bot{token}/sendMessage", botToken)
                    .body(new SendMessageRequest(chatId, text))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private String reminderText(UserEntity user, long pendingCount) {
        if (user.getLanguage() != null && user.getLanguage().equalsIgnoreCase("ru")) {
            return "Напоминание: сегодня осталось отметить " + pendingCount + " привычк(и).";
        }
        return "Reminder: you still have " + pendingCount + " habit(s) to complete today.";
    }

    // Что делает: отправляет сообщение или запрос во внешний сервис и возвращает статус.
    // Как делает: формирует внешний HTTP-запрос, отправляет его и обрабатывает возможные ошибки.
    private record SendMessageRequest(Long chat_id, String text) {
    }
}

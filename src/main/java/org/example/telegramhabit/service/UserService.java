package org.example.telegramhabit.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.UserProfileResponse;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class UserService {

    private final UserRepository userRepository;
    private final AvatarStorageService avatarStorageService;

    // Что делает: проверяет входные данные и извлекает нужные значения.
    // Как делает: проводит проверки и возвращает значение, либо бросает исключение при ошибке.
    public UserEntity requireById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public UserProfileResponse profile(UUID userId) {
        UserEntity user = requireById(userId);
        return new UserProfileResponse(
                user.getId(),
                user.getTelegramId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhotoUrl(),
                user.getLanguage()
        );
    }

    @Transactional
    // Что делает: преобразует или обновляет данные по правилам сервиса.
    // Как делает: применяет правила преобразования, затем сохраняет или возвращает обновлённые данные.
    public UserProfileResponse updateLanguage(UUID userId, String language) {
        UserEntity user = requireById(userId);
        String normalized = normalizeLanguage(language);
        user.setLanguage(normalized);
        userRepository.save(user);
        return profile(userId);
    }

    @Transactional
    // Что делает: создаёт или сохраняет данные и возвращает результат операции.
    // Как делает: валидирует вход, заполняет поля, сохраняет в БД или хранилище и возвращает итог.
    public UserProfileResponse uploadAvatar(UUID userId, MultipartFile file) {
        UserEntity user = requireById(userId);
        String avatarUrl = avatarStorageService.saveAvatar(file);
        user.setPhotoUrl(avatarUrl);
        userRepository.save(user);
        return profile(userId);
    }

    // Что делает: преобразует или обновляет данные по правилам сервиса.
    // Как делает: применяет правила преобразования, затем сохраняет или возвращает обновлённые данные.
    private String normalizeLanguage(String raw) {
        if (raw == null) {
            return "en";
        }
        String lang = raw.trim().toLowerCase();
        return switch (lang) {
            case "ru", "en" -> lang;
            default -> "en";
        };
    }
}

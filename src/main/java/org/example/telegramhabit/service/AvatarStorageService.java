package org.example.telegramhabit.service;

import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.entity.StoredImageEntity;
import org.example.telegramhabit.repository.StoredImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class AvatarStorageService {

    private static final long MAX_AVATAR_SIZE = 5L * 1024L * 1024L;
    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");

    private final StoredImageRepository storedImageRepository;

    @Transactional
    // Что делает: создаёт или сохраняет данные и возвращает результат операции.
    // Как делает: валидирует вход, заполняет поля, сохраняет в БД или хранилище и возвращает итог.
    public String saveAvatar(MultipartFile file) {
        return saveImage(file, "Cannot store avatar file");
    }

    @Transactional
    // Что делает: создаёт или сохраняет данные и возвращает результат операции.
    // Как делает: валидирует вход, заполняет поля, сохраняет в БД или хранилище и возвращает итог.
    public String saveHabitImage(MultipartFile file) {
        return saveImage(file, "Cannot store habit image");
    }

    @Transactional(readOnly = true)
    // Что делает: проверяет входные данные и извлекает нужные значения.
    // Как делает: проводит проверки и возвращает значение, либо бросает исключение при ошибке.
    public StoredImageEntity requireImage(UUID imageId) {
        return storedImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));
    }

    // Что делает: создаёт или сохраняет данные и возвращает результат операции.
    // Как делает: валидирует вход, заполняет поля, сохраняет в БД или хранилище и возвращает итог.
    private String saveImage(MultipartFile file, String storeErrorMessage) {
        validate(file);
        String contentType = resolveContentType(file.getOriginalFilename(), file.getContentType());
        UUID imageId = UUID.randomUUID();
        try {
            StoredImageEntity image = new StoredImageEntity();
            image.setId(imageId);
            image.setContentType(contentType);
            image.setData(file.getBytes());
            image.setCreatedAt(LocalDateTime.now());
            storedImageRepository.save(image);
        } catch (IOException ex) {
            throw new IllegalStateException(storeErrorMessage);
        }
        return "/api/files/" + imageId;
    }

    // Что делает: проверяет входные данные и извлекает нужные значения.
    // Как делает: проводит проверки и возвращает значение, либо бросает исключение при ошибке.
    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Avatar file is required");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new IllegalArgumentException("Avatar is too large (max 5MB)");
        }
    }

    // Что делает: преобразует или обновляет данные по правилам сервиса.
    // Как делает: применяет правила преобразования, затем сохраняет или возвращает обновлённые данные.
    private String resolveContentType(String originalFilename, String contentType) {
        String ext = extractExt(originalFilename);
        if (!ext.isBlank() && ALLOWED_EXT.contains(ext)) {
            return mapExtToContentType(ext);
        }
        String fromMime = mapMime(contentType);
        if (fromMime != null) {
            return fromMime;
        }
        throw new IllegalArgumentException("Unsupported avatar format");
    }

    // Что делает: проверяет входные данные и извлекает нужные значения.
    // Как делает: проводит проверки и возвращает значение, либо бросает исключение при ошибке.
    private String extractExt(String name) {
        if (name == null) {
            return "";
        }
        int idx = name.lastIndexOf('.');
        if (idx < 0 || idx == name.length() - 1) {
            return "";
        }
        return name.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    // Что делает: преобразует или обновляет данные по правилам сервиса.
    // Как делает: применяет правила преобразования, затем сохраняет или возвращает обновлённые данные.
    private String mapMime(String contentType) {
        if (contentType == null) {
            return null;
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> "image/jpeg";
            case "image/png" -> "image/png";
            case "image/webp" -> "image/webp";
            default -> null;
        };
    }

    // Что делает: преобразует или обновляет данные по правилам сервиса.
    // Как делает: применяет правила преобразования, затем сохраняет или возвращает обновлённые данные.
    private String mapExtToContentType(String ext) {
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> throw new IllegalArgumentException("Unsupported avatar format");
        };
    }
}

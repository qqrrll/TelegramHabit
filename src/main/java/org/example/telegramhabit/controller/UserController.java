package org.example.telegramhabit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.UpdateLanguageRequest;
import org.example.telegramhabit.dto.UserProfileResponse;
import org.example.telegramhabit.security.SecurityUtils;
import org.example.telegramhabit.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public UserProfileResponse me() {
        return userService.profile(SecurityUtils.currentUserId());
    }

    @PatchMapping("/language")
    // Что делает: преобразует или обновляет данные по правилам сервиса.
    // Как делает: применяет правила преобразования, затем сохраняет или возвращает обновлённые данные.
    public UserProfileResponse updateLanguage(@Valid @RequestBody UpdateLanguageRequest request) {
        return userService.updateLanguage(SecurityUtils.currentUserId(), request.language());
    }

    @PutMapping("/language")
    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public UserProfileResponse putLanguage(@Valid @RequestBody UpdateLanguageRequest request) {
        return userService.updateLanguage(SecurityUtils.currentUserId(), request.language());
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // Что делает: создаёт или сохраняет данные и возвращает результат операции.
    // Как делает: валидирует вход, заполняет поля, сохраняет в БД или хранилище и возвращает итог.
    public UserProfileResponse uploadAvatar(@RequestPart("file") MultipartFile file) {
        return userService.uploadAvatar(SecurityUtils.currentUserId(), file);
    }
}

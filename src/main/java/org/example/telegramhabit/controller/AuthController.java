package org.example.telegramhabit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.AuthResponse;
import org.example.telegramhabit.dto.DevAuthRequest;
import org.example.telegramhabit.dto.TelegramAuthRequest;
import org.example.telegramhabit.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class AuthController {

    private final AuthService authService;

    @Value("${app.security.dev-auth-enabled:false}")
    private boolean devAuthEnabled;

    @PostMapping("/telegram")
    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public AuthResponse telegram(@Valid @RequestBody TelegramAuthRequest request) {
        return authService.authenticateTelegram(request.initData());
    }

    @PostMapping("/dev")
    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public AuthResponse dev(@Valid @RequestBody DevAuthRequest request) {
        if (!devAuthEnabled) {
            throw new IllegalStateException("Dev auth is disabled");
        }
        return authService.authenticateDev(request.telegramId(), request.firstName(), request.username());
    }
}

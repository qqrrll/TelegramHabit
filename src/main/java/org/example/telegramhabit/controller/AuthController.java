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

/** Public endpoint for Telegram authentication. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Value("${app.security.dev-auth-enabled:false}")
    private boolean devAuthEnabled;

    @PostMapping("/telegram")
    public AuthResponse telegram(@Valid @RequestBody TelegramAuthRequest request) {
        return authService.authenticateTelegram(request.initData());
    }

    @PostMapping("/dev")
    public AuthResponse dev(@Valid @RequestBody DevAuthRequest request) {
        if (!devAuthEnabled) {
            throw new IllegalStateException("Dev auth is disabled");
        }
        return authService.authenticateDev(request.telegramId(), request.firstName(), request.username());
    }
}

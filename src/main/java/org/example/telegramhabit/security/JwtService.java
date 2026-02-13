package org.example.telegramhabit.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class JwtService {

    @Value("${app.security.jwt-secret}")
    private String secret;

    @Value("${app.security.jwt-expiration-minutes}")
    private long expirationMinutes;

    private SecretKey secretKey;

    @PostConstruct
    void init() {
        this.secretKey = Keys.hmacShaKeyFor(resolveSecret(secret));
    }

    // Что делает: создаёт или сохраняет данные и возвращает результат операции.
    // Как делает: валидирует вход, заполняет поля, сохраняет в БД или хранилище и возвращает итог.
    public String generate(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(secretKey)
                .compact();
    }

    // Что делает: проверяет входные данные и извлекает нужные значения.
    // Как делает: проводит проверки и возвращает значение, либо бросает исключение при ошибке.
    public UUID extractUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return UUID.fromString(claims.getSubject());
    }

    // Что делает: преобразует или обновляет данные по правилам сервиса.
    // Как делает: применяет правила преобразования, затем сохраняет или возвращает обновлённые данные.
    private byte[] resolveSecret(String value) {
        if (value.matches("^[A-Za-z0-9+/=]+$") && value.length() >= 44) {
            try {
                return Decoders.BASE64.decode(value);
            } catch (IllegalArgumentException ignored) {
                return value.getBytes(StandardCharsets.UTF_8);
            }
        }
        return value.getBytes(StandardCharsets.UTF_8);
    }
}

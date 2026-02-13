package org.example.telegramhabit.controller;

import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.entity.StoredImageEntity;
import org.example.telegramhabit.service.AvatarStorageService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class FileController {

    private final AvatarStorageService avatarStorageService;

    @GetMapping("/{id}")
    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    public ResponseEntity<byte[]> get(@PathVariable UUID id) {
        StoredImageEntity image = avatarStorageService.requireImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .body(image.getData());
    }
}

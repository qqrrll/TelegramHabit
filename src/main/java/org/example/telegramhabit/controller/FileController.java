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

/** Serves uploaded files stored in database. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private final AvatarStorageService avatarStorageService;

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> get(@PathVariable UUID id) {
        StoredImageEntity image = avatarStorageService.requireImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
                .body(image.getData());
    }
}

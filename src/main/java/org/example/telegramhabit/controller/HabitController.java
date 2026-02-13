package org.example.telegramhabit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.HabitCompletionResponse;
import org.example.telegramhabit.dto.HabitRequest;
import org.example.telegramhabit.dto.HabitResponse;
import org.example.telegramhabit.dto.HabitStatsResponse;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.security.SecurityUtils;
import org.example.telegramhabit.service.HabitCompletionService;
import org.example.telegramhabit.service.HabitService;
import org.example.telegramhabit.service.UserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/habits")
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class HabitController {

    private final HabitService habitService;
    private final HabitCompletionService completionService;
    private final UserService userService;

    @GetMapping
    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    public List<HabitResponse> list() {
        return habitService.list(currentUser());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    // Что делает: создаёт или сохраняет данные и возвращает результат операции.
    // Как делает: валидирует вход, заполняет поля, сохраняет в БД или хранилище и возвращает итог.
    public HabitResponse create(@Valid @RequestBody HabitRequest request) {
        return habitService.create(currentUser(), request);
    }

    @PutMapping("/{id}")
    // Что делает: преобразует или обновляет данные по правилам сервиса.
    // Как делает: применяет правила преобразования, затем сохраняет или возвращает обновлённые данные.
    public HabitResponse update(@PathVariable UUID id, @Valid @RequestBody HabitRequest request) {
        return habitService.update(currentUser(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // Что делает: удаляет данные по условиям метода с учётом связей.
    // Как делает: проверяет доступ и существование сущности, затем удаляет связанные и целевые записи.
    public void delete(@PathVariable UUID id) {
        habitService.delete(currentUser(), id);
    }

    @PostMapping("/{id}/complete")
    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public HabitCompletionResponse complete(@PathVariable UUID id) {
        return completionService.complete(currentUser(), id);
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // Что делает: создаёт или сохраняет данные и возвращает результат операции.
    // Как делает: валидирует вход, заполняет поля, сохраняет в БД или хранилище и возвращает итог.
    public HabitResponse uploadImage(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        return habitService.uploadImage(currentUser(), id, file);
    }

    @DeleteMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public void uncomplete(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        completionService.uncomplete(currentUser(), id, date);
    }

    @GetMapping("/{id}/history")
    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    public List<HabitCompletionResponse> history(@PathVariable UUID id) {
        return completionService.history(currentUser(), id);
    }

    @GetMapping("/{id}/stats")
    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    public HabitStatsResponse stats(@PathVariable UUID id) {
        return habitService.stats(currentUser(), id);
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private UserEntity currentUser() {
        return userService.requireById(SecurityUtils.currentUserId());
    }
}

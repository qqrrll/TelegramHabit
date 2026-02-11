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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Protected endpoints for habits, completions and stats. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/habits")
public class HabitController {

    private final HabitService habitService;
    private final HabitCompletionService completionService;
    private final UserService userService;

    @GetMapping
    public List<HabitResponse> list() {
        return habitService.list(currentUser());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HabitResponse create(@Valid @RequestBody HabitRequest request) {
        return habitService.create(currentUser(), request);
    }

    @PutMapping("/{id}")
    public HabitResponse update(@PathVariable UUID id, @Valid @RequestBody HabitRequest request) {
        return habitService.update(currentUser(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        habitService.delete(currentUser(), id);
    }

    @PostMapping("/{id}/complete")
    public HabitCompletionResponse complete(@PathVariable UUID id) {
        return completionService.complete(currentUser(), id);
    }

    @DeleteMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uncomplete(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        completionService.uncomplete(currentUser(), id, date);
    }

    @GetMapping("/{id}/history")
    public List<HabitCompletionResponse> history(@PathVariable UUID id) {
        return completionService.history(currentUser(), id);
    }

    @GetMapping("/{id}/stats")
    public HabitStatsResponse stats(@PathVariable UUID id) {
        return habitService.stats(currentUser(), id);
    }

    private UserEntity currentUser() {
        return userService.requireById(SecurityUtils.currentUserId());
    }
}

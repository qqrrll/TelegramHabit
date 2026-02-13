package org.example.telegramhabit.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.AcceptFriendInviteRequest;
import org.example.telegramhabit.dto.FriendInviteResponse;
import org.example.telegramhabit.dto.HabitReactionRequest;
import org.example.telegramhabit.dto.HabitReactionSummaryResponse;
import org.example.telegramhabit.dto.FriendResponse;
import org.example.telegramhabit.dto.HabitResponse;
import org.example.telegramhabit.dto.HabitStatsResponse;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.security.SecurityUtils;
import org.example.telegramhabit.service.FriendService;
import org.example.telegramhabit.service.HabitReactionService;
import org.example.telegramhabit.service.HabitService;
import org.example.telegramhabit.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
// Что делает: описывает ключевой компонент backend-слоя приложения.
// Как делает: объявляет структуру и контракт, который используют остальные части системы.
public class FriendController {

    private final FriendService friendService;
    private final HabitReactionService habitReactionService;
    private final HabitService habitService;
    private final UserService userService;

    @GetMapping
    // Что делает: читает и возвращает данные для API или внутренней логики.
    // Как делает: делает запрос к репозиторию, при необходимости фильтрует и маппит результат.
    public List<FriendResponse> list() {
        return friendService.listFriends(currentUser());
    }

    @PostMapping("/invite")
    // Что делает: создаёт или сохраняет данные и возвращает результат операции.
    // Как делает: валидирует вход, заполняет поля, сохраняет в БД или хранилище и возвращает итог.
    public FriendInviteResponse createInvite() {
        return friendService.createInvite(currentUser());
    }

    @PostMapping("/accept")
    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public FriendResponse acceptInvite(@Valid @RequestBody AcceptFriendInviteRequest request) {
        return friendService.acceptInvite(currentUser(), request.code());
    }

    @DeleteMapping("/{friendId}")
    // Что делает: удаляет данные по условиям метода с учётом связей.
    // Как делает: проверяет доступ и существование сущности, затем удаляет связанные и целевые записи.
    public void remove(@PathVariable UUID friendId) {
        friendService.removeFriend(currentUser(), friendId);
    }

    @GetMapping("/{friendId}/profile")
    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public FriendResponse profile(@PathVariable UUID friendId) {
        return friendService.friendProfile(currentUser(), friendId);
    }

    @GetMapping("/{friendId}/habits")
    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public List<HabitResponse> habits(@PathVariable UUID friendId) {
        UserEntity friend = friendService.requireFriend(currentUser(), friendId);
        return habitService.listByOwner(friend);
    }

    @GetMapping("/{friendId}/habits/{habitId}/stats")
    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public HabitStatsResponse habitStats(@PathVariable UUID friendId, @PathVariable UUID habitId) {
        UserEntity friend = friendService.requireFriend(currentUser(), friendId);
        return habitService.statsByOwner(friend, habitId);
    }

    @GetMapping("/{friendId}/habits/{habitId}/reactions")
    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    public List<HabitReactionSummaryResponse> habitReactions(@PathVariable UUID friendId, @PathVariable UUID habitId) {
        return habitReactionService.listForFriendHabit(currentUser(), friendId, habitId);
    }

    @PostMapping("/{friendId}/habits/{habitId}/reactions")
    // Что делает: преобразует или обновляет данные по правилам сервиса.
    // Как делает: применяет правила преобразования, затем сохраняет или возвращает обновлённые данные.
    public List<HabitReactionSummaryResponse> toggleHabitReaction(
            @PathVariable UUID friendId,
            @PathVariable UUID habitId,
            @Valid @RequestBody HabitReactionRequest request
    ) {
        return habitReactionService.toggleForFriendHabit(currentUser(), friendId, habitId, request.emoji());
    }

    @DeleteMapping("/{friendId}/habits/{habitId}/reactions")
    // Что делает: удаляет данные по условиям метода с учётом связей.
    // Как делает: проверяет доступ и существование сущности, затем удаляет связанные и целевые записи.
    public List<HabitReactionSummaryResponse> removeHabitReaction(
            @PathVariable UUID friendId,
            @PathVariable UUID habitId,
            @RequestParam String emoji
    ) {
        return habitReactionService.removeForFriendHabit(currentUser(), friendId, habitId, emoji);
    }

    // Что делает: выполняет бизнес-операцию метода и возвращает ожидаемый результат.
    // Как делает: выполняет шаги бизнес-логики по месту и возвращает итоговое значение.
    private UserEntity currentUser() {
        return userService.requireById(SecurityUtils.currentUserId());
    }
}

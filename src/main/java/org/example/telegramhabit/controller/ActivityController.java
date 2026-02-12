package org.example.telegramhabit.controller;

import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.ActivityResponse;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.security.SecurityUtils;
import org.example.telegramhabit.service.ActivityService;
import org.example.telegramhabit.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Returns activity feed for authenticated user and their friends. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/activity")
public class ActivityController {

    private final ActivityService activityService;
    private final UserService userService;

    @GetMapping
    public List<ActivityResponse> list() {
        UserEntity user = userService.requireById(SecurityUtils.currentUserId());
        return activityService.list(user);
    }
}

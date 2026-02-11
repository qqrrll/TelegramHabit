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

/** Profile endpoints for language and personal data. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserProfileResponse me() {
        return userService.profile(SecurityUtils.currentUserId());
    }

    @PatchMapping("/language")
    public UserProfileResponse updateLanguage(@Valid @RequestBody UpdateLanguageRequest request) {
        return userService.updateLanguage(SecurityUtils.currentUserId(), request.language());
    }

    @PutMapping("/language")
    public UserProfileResponse putLanguage(@Valid @RequestBody UpdateLanguageRequest request) {
        return userService.updateLanguage(SecurityUtils.currentUserId(), request.language());
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserProfileResponse uploadAvatar(@RequestPart("file") MultipartFile file) {
        return userService.uploadAvatar(SecurityUtils.currentUserId(), file);
    }
}

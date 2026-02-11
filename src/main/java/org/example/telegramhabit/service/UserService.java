package org.example.telegramhabit.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.dto.UserProfileResponse;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/** Utility service for loading the authenticated user. */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AvatarStorageService avatarStorageService;

    public UserEntity requireById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse profile(UUID userId) {
        UserEntity user = requireById(userId);
        return new UserProfileResponse(
                user.getId(),
                user.getTelegramId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhotoUrl(),
                user.getLanguage()
        );
    }

    @Transactional
    public UserProfileResponse updateLanguage(UUID userId, String language) {
        UserEntity user = requireById(userId);
        String normalized = normalizeLanguage(language);
        user.setLanguage(normalized);
        userRepository.save(user);
        return profile(userId);
    }

    @Transactional
    public UserProfileResponse uploadAvatar(UUID userId, MultipartFile file) {
        UserEntity user = requireById(userId);
        String avatarUrl = avatarStorageService.saveAvatar(file);
        user.setPhotoUrl(avatarUrl);
        userRepository.save(user);
        return profile(userId);
    }

    private String normalizeLanguage(String raw) {
        if (raw == null) {
            return "en";
        }
        String lang = raw.trim().toLowerCase();
        return switch (lang) {
            case "ru", "en" -> lang;
            default -> "en";
        };
    }
}

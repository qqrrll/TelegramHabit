package org.example.telegramhabit.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.telegramhabit.entity.UserEntity;
import org.example.telegramhabit.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Utility service for loading the authenticated user. */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserEntity requireById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}

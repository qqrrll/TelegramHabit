package org.example.telegramhabit.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Handles local avatar file persistence and returns public URL. */
@Service
public class AvatarStorageService {

    private static final long MAX_AVATAR_SIZE = 5L * 1024L * 1024L;
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");

    private final Path uploadDir;
    private final String avatarBaseUrl;

    public AvatarStorageService(
            @Value("${app.storage.upload-dir:uploads}") String uploadDir,
            @Value("${app.storage.avatar-base-url:/uploads}") String avatarBaseUrl
    ) {
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
        this.avatarBaseUrl = avatarBaseUrl.endsWith("/") ? avatarBaseUrl.substring(0, avatarBaseUrl.length() - 1) : avatarBaseUrl;
    }

    public String saveAvatar(MultipartFile file) {
        validate(file);
        String extension = resolveExtension(file.getOriginalFilename(), file.getContentType());
        String filename = UUID.randomUUID() + "." + extension;
        Path target = uploadDir.resolve(filename).normalize();

        try {
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot store avatar file");
        }

        return avatarBaseUrl + "/" + filename;
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Avatar file is required");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new IllegalArgumentException("Avatar is too large (max 5MB)");
        }
    }

    private String resolveExtension(String originalFilename, String contentType) {
        String ext = extractExt(originalFilename);
        if (!ext.isBlank() && ALLOWED_EXT.contains(ext)) {
            return ext;
        }
        String fromMime = mapMime(contentType);
        if (fromMime != null) {
            return fromMime;
        }
        throw new IllegalArgumentException("Unsupported avatar format");
    }

    private String extractExt(String name) {
        if (name == null) {
            return "";
        }
        int idx = name.lastIndexOf('.');
        if (idx < 0 || idx == name.length() - 1) {
            return "";
        }
        return name.substring(idx + 1).toLowerCase(Locale.ROOT);
    }

    private String mapMime(String contentType) {
        if (contentType == null) {
            return null;
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> null;
        };
    }
}

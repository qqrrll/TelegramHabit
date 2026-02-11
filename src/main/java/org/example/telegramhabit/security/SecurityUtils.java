package org.example.telegramhabit.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

/** Helper methods for authenticated security context access. */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** Extracts current user UUID from Spring Security context. */
    public static UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID userId)) {
            throw new IllegalStateException("Unauthorized");
        }
        return userId;
    }
}

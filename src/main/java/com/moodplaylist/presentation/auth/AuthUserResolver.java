package com.moodplaylist.presentation.auth;

import com.moodplaylist.infrastructure.auth.model.AuthUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthUserResolver {
    private AuthUserResolver() {}

    public static Long currentUserIdOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUser user)) {
            throw new IllegalStateException("unauthorized");
        }
        return user.userId();
    }
}

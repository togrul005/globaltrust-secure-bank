package com.globaltrust.bank.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Small helpers for reading the current principal inside service methods. */
public final class SecurityUtils {

    private SecurityUtils() { }

    public static String currentUsername() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return (a != null) ? a.getName() : "anonymous";
    }

    public static boolean hasRole(String role) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null) return false;
        return a.getAuthorities().stream()
                .anyMatch(g -> g.getAuthority().equals("ROLE_" + role));
    }
}

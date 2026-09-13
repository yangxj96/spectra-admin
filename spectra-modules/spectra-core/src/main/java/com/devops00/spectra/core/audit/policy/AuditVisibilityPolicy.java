/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit.policy;

import com.devops00.spectra.common.audit.AuditRecord;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Locale;
import java.util.UUID;

/** Read-visibility rules for rows in the unified audit table. */
public interface AuditVisibilityPolicy {

    boolean canView(Authentication viewer, AuditRecord event);

    default boolean canViewHighRisk(Authentication viewer) {
        return hasAuthority(viewer, "ROLE_DEV_OPS", "DEV_OPS", "ROLE_BREAK_GLASS", "BREAK_GLASS",
                "security:root:manage");
    }

    default boolean canViewAllNonHighRisk(Authentication viewer) {
        return canViewHighRisk(viewer)
                || hasAuthority(viewer, "ROLE_SYSTEM_ADMIN", "SYSTEM_ADMIN", "system:admin");
    }

    default UUID viewerId(Authentication viewer) {
        if (viewer == null || viewer.getPrincipal() == null) {
            return null;
        }
        Object principal = viewer.getPrincipal();
        if (principal instanceof UUID id) {
            return id;
        }
        try {
            return UUID.fromString(String.valueOf(principal));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    default boolean isHighRiskEvent(String eventType) {
        if (eventType == null) {
            return true;
        }
        String normalized = eventType.toUpperCase(Locale.ROOT);
        return normalized.contains("ROOT")
                || normalized.contains("BREAK_GLASS")
                || normalized.contains("SECURITY")
                || normalized.contains("SESSION")
                || normalized.contains("PASSWORD")
                || normalized.contains("AUDIT");
    }

    private static boolean hasAuthority(Authentication viewer, String... expected) {
        if (viewer == null || !viewer.isAuthenticated()) {
            return false;
        }
        for (GrantedAuthority authority : viewer.getAuthorities()) {
            if (authority == null || authority.getAuthority() == null) {
                continue;
            }
            for (String candidate : expected) {
                if (candidate.equals(authority.getAuthority())) {
                    return true;
                }
            }
        }
        return false;
    }
}

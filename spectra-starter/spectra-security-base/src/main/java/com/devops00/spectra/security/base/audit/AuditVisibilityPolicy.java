/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.security.base.audit;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

/**
 * Security Audit 查询可见性策略端口。
 * <p>
 * 查询端只能依赖该策略，不得在 Controller/Service 中散落 Root 或管理员特判。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@FunctionalInterface
public interface AuditVisibilityPolicy {

    /**
     * 判断当前主体是否可以看到指定审计事件。
     *
     * @param viewer 当前主体
     * @param event  审计事件
     * @return 是否可见
     */
    boolean canView(Authentication viewer, SecurityAuditEvent event);

    /**
     * Root/break-glass 主体是否可以查看高风险安全事件。
     */
    default boolean canViewHighRisk(Authentication viewer) {
        return hasAuthority(viewer, "ROLE_DEV_OPS", "DEV_OPS", "ROLE_BREAK_GLASS", "BREAK_GLASS",
                "security:root:manage");
    }

    /**
     * SYSTEM_ADMIN 可以查看普通安全事件，但不能越过 Root/break-glass 高风险可见性边界。
     */
    default boolean canViewAllNonHighRisk(Authentication viewer) {
        return canViewHighRisk(viewer)
                || hasAuthority(viewer, "ROLE_SYSTEM_ADMIN", "SYSTEM_ADMIN", "system:admin");
    }

    /**
     * 从认证主体中提取稳定用户 ID，用于普通用户的 operator/target 自身可见性过滤。
     */
    default UUID viewerId(Authentication viewer) {
        if (viewer == null || viewer.getPrincipal() == null) {
            return null;
        }
        Object principal = viewer.getPrincipal();
        if (principal instanceof UUID id) {
            return id;
        }
        try {
            return UUID.fromString(principal.toString());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * 高风险事件只允许 Root/break-glass 查看；事件类型由生产者维护为稳定大写名称。
     */
    default boolean isHighRiskEvent(String eventType) {
        if (eventType == null) {
            return true;
        }
        String normalized = eventType.toUpperCase(java.util.Locale.ROOT);
        return normalized.contains("ROOT")
                || normalized.contains("BREAK_GLASS")
                || normalized.contains("SECURITY")
                || normalized.contains("SESSION")
                || normalized.contains("MFA")
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

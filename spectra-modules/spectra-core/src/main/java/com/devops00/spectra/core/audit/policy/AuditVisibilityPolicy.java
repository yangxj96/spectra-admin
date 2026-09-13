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

package com.devops00.spectra.core.audit.policy;

import com.devops00.spectra.common.audit.AuditRecord;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Locale;
import java.util.UUID;

/**
 * 封装审计相关的业务规则和判定策略。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public interface AuditVisibilityPolicy {

    /**
     * 判断视图。
     *
     * @param viewer 当前已认证用户信息。
     * @param event  事件参数。
     * @return 条件判断结果。
     */
    boolean canView(Authentication viewer, AuditRecord event);

    /**
     * 判断视图高风险。
     *
     * @param viewer 当前已认证用户信息。
     * @return 条件判断结果。
     */
    default boolean canViewHighRisk(Authentication viewer) {
        return hasAuthority(viewer, "ROLE_DEV_OPS", "DEV_OPS", "ROLE_BREAK_GLASS", "BREAK_GLASS",
                "security:root:manage");
    }

    /**
     * 判断视图高风险。
     *
     * @param viewer 当前已认证用户信息。
     * @return 条件判断结果。
     */
    default boolean canViewAllNonHighRisk(Authentication viewer) {
        return canViewHighRisk(viewer)
                || hasAuthority(viewer, "ROLE_SYSTEM_ADMIN", "SYSTEM_ADMIN", "system:admin");
    }

    /**
     * 处理当前用户标识相关数据。
     *
     * @param viewer 当前已认证用户信息。
     * @return 生成或查询到的唯一标识。
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
            return UUID.fromString(String.valueOf(principal));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * 判断高风险事件。
     *
     * @param eventType 事件类型参数。
     * @return 条件判断结果。
     */
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

    /**
     * 判断权限。
     *
     * @param viewer   当前已认证用户信息。
     * @param expected 待比较的预期字段值。
     * @return 条件判断结果。
     */
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

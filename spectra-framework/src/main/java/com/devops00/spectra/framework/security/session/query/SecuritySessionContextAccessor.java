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

package com.devops00.spectra.framework.security.session.query;

import com.devops00.spectra.common.config.SystemConfigValueProvider;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.framework.security.session.token.SecurityTokenAccessor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.UUID;

/**
 * 基于窄 Security Session 端口适配业务上下文。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public class SecuritySessionContextAccessor implements SecurityContextAccessor {

    private static final String SYSTEM_DEFAULT_TIMEZONE_KEY = "system.default-timezone";

    private static final String UTC_ZONE_ID = "UTC";

    private final SecuritySessionReader sessionReader;

    private final SecurityTokenAccessor tokenAccessor;

    private final ObjectProvider<SystemConfigValueProvider> systemConfigValueProvider;

    public SecuritySessionContextAccessor(SecuritySessionReader sessionReader, SecurityTokenAccessor tokenAccessor,
                                          ObjectProvider<SystemConfigValueProvider> systemConfigValueProvider) {
        this.sessionReader = sessionReader;
        this.tokenAccessor = tokenAccessor;
        this.systemConfigValueProvider = systemConfigValueProvider;
    }

    /**
     * 读取当前登录主体。
     *
     * @return 返回当前安全 Session 对应的主体；未认证请求返回 null。
     */
    @Override
    public @Nullable SecurityPrincipal currentUser() {
        return sessionReader.getCurrentUser();
    }

    /**
     * 读取当前登录主体 ID。
     *
     * @return 返回当前安全主体的用户 ID；未认证请求返回 null。
     */
    @Override
    public @Nullable UUID currentUserId() {
        SecurityPrincipal user = currentUser();
        return user == null ? null : user.getId();
    }

    /**
     * 读取当前请求访问令牌。
     *
     * @return 返回当前请求携带的访问令牌；请求没有令牌时返回 null。
     */
    @Override
    public @Nullable String currentToken() {
        return tokenAccessor.getCurrentToken();
    }

    /**
     * 读取当前用户时区；无法确认安全主体时按安全策略失败。
     *
     * @return 返回当前用户时区 ID；未认证、用户时区无效或系统配置缺失时依次回退到系统时区和 UTC，不返回 null。
     */
    @Override
    public String currentUserZoneId() {
        SecurityPrincipal user = currentUser();
        var userZoneId = normalizeZoneId(user == null ? null : user.getTimezone());
        if (userZoneId != null) {
            return userZoneId;
        }

        var provider = systemConfigValueProvider.getIfAvailable();
        if (provider != null) {
            var systemZoneId = provider.find(SYSTEM_DEFAULT_TIMEZONE_KEY)
                    .map(this::normalizeZoneId)
                    .orElse(null);
            if (systemZoneId != null) {
                return systemZoneId;
            }
        }
        return UTC_ZONE_ID;
    }

    /**
     * 转换、解析或规范化数据（{@code normalizeZoneId}）。
     */
    private String normalizeZoneId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        var zoneId = value.trim();
        try {
            ZoneId.of(zoneId);
            return zoneId;
        } catch (DateTimeException ignored) {
            return null;
        }
    }

    /**
     * 读取当前认证主体的用户名。
     *
     * @return 返回当前认证主体用户名；没有认证主体时返回固定占位文本“未找到用户名”，不返回 null。
     */
    @Override
    public String currentUsername() {
        SecurityPrincipal user = currentUser();
        return user != null ? user.getUsername() : "未找到用户名";
    }
}

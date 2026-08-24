/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.starter.holder;

import com.devops00.spectra.common.config.SystemConfigValueProvider;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.holder.SecuritySessionReader;
import com.devops00.spectra.security.base.holder.SecurityTokenAccessor;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.UUID;

/** 基于窄 Security Session 端口适配业务上下文。 */
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

    @Override
    public @Nullable SecurityUser currentUser() {
        return sessionReader.getCurrentUser();
    }

    @Override
    public @Nullable UUID currentUserId() {
        SecurityUser user = currentUser();
        return user == null ? null : user.getId();
    }

    @Override
    public @Nullable String currentToken() {
        return tokenAccessor.getCurrentToken();
    }

    @Override
    public String currentUserZoneId() {
        SecurityUser user = currentUser();
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

    @Override
    public String currentUsername() {
        SecurityUser user = currentUser();
        return user != null ? user.getUsername() : "未找到用户名";
    }
}

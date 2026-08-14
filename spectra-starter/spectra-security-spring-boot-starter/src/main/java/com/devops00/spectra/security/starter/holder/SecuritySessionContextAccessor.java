/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.starter.holder;

import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.holder.SecuritySessionReader;
import com.devops00.spectra.security.base.holder.SecurityTokenAccessor;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/** 基于窄 Security Session 端口适配业务上下文。 */
public class SecuritySessionContextAccessor implements SecurityContextAccessor {

    private final SecuritySessionReader sessionReader;

    private final SecurityTokenAccessor tokenAccessor;

    public SecuritySessionContextAccessor(SecuritySessionReader sessionReader, SecurityTokenAccessor tokenAccessor) {
        this.sessionReader = sessionReader;
        this.tokenAccessor = tokenAccessor;
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
        return user != null && user.getTimezone() != null ? user.getTimezone() : "UTC";
    }

    @Override
    public String currentUsername() {
        SecurityUser user = currentUser();
        return user != null ? user.getUsername() : "未找到用户名";
    }
}

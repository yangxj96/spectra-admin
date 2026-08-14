/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.starter.holder;

import com.devops00.spectra.security.base.holder.SecHolderStrategy;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/** 基于当前 SecuritySession 适配业务上下文端口。 */
public class SecHolderSecurityContextAccessor implements SecurityContextAccessor {

    private final SecHolderStrategy strategy;

    public SecHolderSecurityContextAccessor(SecHolderStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public @Nullable SecurityUser currentUser() {
        return strategy.getCurrentUser();
    }

    @Override
    public @Nullable UUID currentUserId() {
        return strategy.getCurrentUserId();
    }

    @Override
    public @Nullable String currentToken() {
        return strategy.getCurrentToken();
    }

    @Override
    public String currentUserZoneId() {
        return strategy.getCurrentUserZoneId();
    }

    @Override
    public String currentUsername() {
        return strategy.getCurrentUsername();
    }
}

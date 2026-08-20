/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.authorization.controller;

import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationContextVO;
import com.devops00.spectra.security.base.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 当前认证主体的授权上下文接口。
 */
@RestController
@RequestMapping("/security/context")
public class SecurityContextController {

    private final AuthorizationSnapshotProvider authorizationSnapshotProvider;

    private final SecurityContextAccessor securityContextAccessor;

    public SecurityContextController(AuthorizationSnapshotProvider authorizationSnapshotProvider,
                                     SecurityContextAccessor securityContextAccessor) {
        this.authorizationSnapshotProvider = authorizationSnapshotProvider;
        this.securityContextAccessor = securityContextAccessor;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public AuthorizationContextVO current() {
        UUID userId = securityContextAccessor.currentUserId();
        if (userId == null) {
            throw new AccessDeniedException("当前认证主体缺少用户标识");
        }
        var snapshot = authorizationSnapshotProvider.load(userId);
        return new AuthorizationContextVO(snapshot.permissions(), snapshot.grantablePermissions());
    }
}

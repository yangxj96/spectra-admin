/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.common.security.authorization;

import java.util.UUID;

/**
 * Permission-specific 的资源授权门面。
 *
 * <p>所有判断都从同一个 Permission 的 Boundary 读取，禁止将不同 Permission 的 Scope
 * 先做全局 UNION 后重新组合。</p>
 */
public record ScopedAuthorization(UUID subjectId, AuthorizationSnapshot snapshot) {

    public ScopedAuthorization {
        if (subjectId == null) {
            throw new IllegalArgumentException("subjectId 不能为空");
        }
        if (snapshot == null) {
            throw new IllegalArgumentException("authorization snapshot 不能为空");
        }
    }

    /**
     * 判断条件是否满足（{@code hasPermission}）。
     */
    public boolean hasPermission(String permission) {
        return snapshot.hasPermission(permission);
    }

    /**
     * 查询或获取目标数据（{@code allows}）。
     */
    public boolean allows(ExecutionContext context, ScopeQuery query) {
        return context != null
                && subjectId.equals(context.subjectId())
                && snapshot.canAccess(context.permission(), query);
    }

    /**
     * 查询或获取目标数据（{@code allows}）。
     */
    public boolean allows(String permission, ScopeQuery query) {
        return permission != null && snapshot.canAccess(permission, query);
    }
}

/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.authorization.javabean.vo;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 当前用户授权上下文。仅返回稳定权限编码，不返回角色名称或旧权限对象。
 */
public record AuthorizationContextVO(Set<String> permissions, Set<String> grantablePermissions) {

    public AuthorizationContextVO {
        permissions = immutableSet(permissions);
        grantablePermissions = immutableSet(grantablePermissions);
    }

    @Override
    public Set<String> permissions() {
        return immutableSet(permissions);
    }

    @Override
    public Set<String> grantablePermissions() {
        return immutableSet(grantablePermissions);
    }

    /**
     * 转换、解析或规范化数据（{@code immutableSet}）。
     */
    private static Set<String> immutableSet(Set<String> source) {
        return source == null ? Set.of() : Collections.unmodifiableSet(new LinkedHashSet<>(source));
    }
}

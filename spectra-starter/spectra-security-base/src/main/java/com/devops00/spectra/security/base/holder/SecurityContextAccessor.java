/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.base.holder;

import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * 业务层读取当前安全上下文的窄端口；会话、Token 和 Redis 细节留在适配层。
 */
public interface SecurityContextAccessor {

    /**
     * 查询或获取目标数据（{@code currentUser}）。
     */
    @Nullable
    SecurityUser currentUser();

    /**
     * 查询或获取目标数据（{@code currentUserId}）。
     */
    @Nullable
    UUID currentUserId();

    /**
     * 查询或获取目标数据（{@code currentToken}）。
     */
    @Nullable
    String currentToken();

    /**
     * 获取当前请求的有效时区，按用户时区、系统默认时区、UTC 的顺序解析。
     *
     * @return 有效时区 ID
     */
    String currentUserZoneId();

    /**
     * 查询或获取目标数据（{@code currentUsername}）。
     */
    String currentUsername();
}

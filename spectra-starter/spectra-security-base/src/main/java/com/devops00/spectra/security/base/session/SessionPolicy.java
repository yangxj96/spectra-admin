/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.base.session;

/**
 * 登录端的会话策略快照。
 *
 * @param concurrencyMode    并发模式
 * @param maxSessions        最大活动会话数
 * @param accessTtlSeconds   Access TTL
 * @param refreshTtlSeconds  Refresh TTL
 * @param absoluteTtlSeconds 绝对 TTL，可为空
 * @param idleTtlSeconds     空闲 TTL，可为空
 */
public record SessionPolicy(SessionConcurrencyMode concurrencyMode, int maxSessions, long accessTtlSeconds,
                            long refreshTtlSeconds, Long absoluteTtlSeconds, Long idleTtlSeconds) {

    public SessionPolicy {
        if (concurrencyMode == null || maxSessions < 1 || accessTtlSeconds < 1 || refreshTtlSeconds < 1) {
            throw new IllegalArgumentException("会话策略参数无效");
        }
        if (absoluteTtlSeconds != null && absoluteTtlSeconds < 1
                || idleTtlSeconds != null && idleTtlSeconds < 1) {
            throw new IllegalArgumentException("会话可选TTL必须为正数");
        }
    }

    /**
     * 查询或获取目标数据（{@code defaults}）。
     */
    public static SessionPolicy defaults(long accessTtlSeconds, long refreshTtlSeconds) {
        return new SessionPolicy(SessionConcurrencyMode.ALLOW, 5, accessTtlSeconds, refreshTtlSeconds, null, null);
    }
}

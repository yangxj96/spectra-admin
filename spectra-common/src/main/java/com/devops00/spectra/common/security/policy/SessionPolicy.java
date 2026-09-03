/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.common.security.policy;

/**
 * 登录端会话策略快照。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
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

    /** 使用给定的 Access/Refresh TTL 创建允许并发的默认策略。 */
    public static SessionPolicy defaults(long accessTtlSeconds, long refreshTtlSeconds) {
        return new SessionPolicy(SessionConcurrencyMode.ALLOW, 5, accessTtlSeconds, refreshTtlSeconds, null, null);
    }
}

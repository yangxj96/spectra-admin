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

package com.devops00.spectra.framework.security.session;

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.common.security.policy.SessionPolicy;
import com.devops00.spectra.framework.security.redis.value.SecurityRedisValueParser;

import java.time.Duration;
import java.util.Map;

/**
 * 一个 Token Family 的原始登录时间与最后活动时间，供 Access 和 Refresh 共同判定期限。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/10
 */
record SecuritySessionLifetime(long loginTime, long lastActiveTime) {

    static SecuritySessionLifetime from(Map<?, ?> state) {
        long loginTime = SecurityRedisValueParser.requiredLong(state.get("loginTime"), "Session.loginTime");
        long lastActiveTime = SecurityRedisValueParser.requiredLong(state.get("lastActiveTime"), "Session.lastActiveTime");
        if (loginTime < 1 || lastActiveTime < loginTime) {
            throw new SecurityRedisUnavailableException("安全 Redis 会话时间状态无效", null);
        }
        return new SecuritySessionLifetime(loginTime, lastActiveTime);
    }

    boolean expired(SessionPolicy policy, long now) {
        if (now < lastActiveTime) {
            throw new SecurityRedisUnavailableException("安全 Redis 会话时间晚于当前时钟", null);
        }
        return remaining(policy.absoluteTtlSeconds(), now - loginTime) <= 0
                || remaining(policy.idleTtlSeconds(), now - lastActiveTime) <= 0;
    }

    SecuritySessionLifetime touch(long now) {
        return new SecuritySessionLifetime(loginTime, Math.max(lastActiveTime, now));
    }

    Duration accessTtl(SessionPolicy policy, long now) {
        return ttl(policy.accessTtlSeconds(), policy, now);
    }

    Duration refreshTtl(SessionPolicy policy, long now) {
        return ttl(policy.refreshTtlSeconds(), policy, now);
    }

    Duration until(long tokenDeadline, SessionPolicy policy, long now) {
        if (tokenDeadline <= now) {
            return Duration.ZERO;
        }
        long millis = Math.min(tokenDeadline - now, remaining(policy.absoluteTtlSeconds(), now - loginTime));
        millis = Math.min(millis, remaining(policy.idleTtlSeconds(), now - lastActiveTime));
        return Duration.ofMillis(millis);
    }

    static long deadline(long now, long ttlSeconds) {
        return Math.addExact(now, Math.multiplyExact(ttlSeconds, 1000));
    }

    private Duration ttl(long tokenSeconds, SessionPolicy policy, long now) {
        long millis = Math.multiplyExact(tokenSeconds, 1000);
        millis = Math.min(millis, remaining(policy.absoluteTtlSeconds(), now - loginTime));
        millis = Math.min(millis, remaining(policy.idleTtlSeconds(), now - lastActiveTime));
        return Duration.ofMillis(millis);
    }

    private static long remaining(Long limitSeconds, long elapsedMillis) {
        return limitSeconds == null ? Long.MAX_VALUE : Math.multiplyExact(limitSeconds, 1000) - elapsedMillis;
    }
}

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
import com.devops00.spectra.framework.security.redis.key.SecurityRedisExecutor;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.token.TokenDigestService;
import com.devops00.spectra.framework.security.redis.value.SecurityRedisValueParser;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Objects;

/**
 * 管理端不透明会话句柄的安全 Redis 映射。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@NullMarked
final class SecuritySessionHandleStore {

    private final RedisTemplate<String, Object> redis;

    SecuritySessionHandleStore(RedisTemplate<String, Object> redis) {
        this.redis = Objects.requireNonNull(redis, "redis");
    }

    /**
     * 为 Refresh Token Family 创建句柄，或延长并返回该 Family 已有的句柄。
     *
     * @param familyId 会话所属的内部 Family 标识。
     * @param ttl      句柄索引允许存活的时长。
     * @return 返回随机、不用于认证且仅通过摘要作为 Redis 键的会话句柄。
     */
    String createOrGet(String familyId, Duration ttl) {
        requireFamilyAndTtl(familyId, ttl);
        return SecurityRedisExecutor.execute("维护安全会话句柄", () -> createOrGetInternal(familyId, ttl));
    }

    /**
     * 为仍有效的旧 Family 补充管理端会话句柄。
     *
     * @param familyId 会话所属的内部 Family 标识。
     * @param ttl      当前会话摘要的剩余时长，确保旧摘要和新句柄同时过期。
     * @return 返回该 Family 稳定的随机会话句柄。
     */
    String ensureExisting(String familyId, Duration ttl) {
        return createOrGet(familyId, ttl);
    }

    /**
     * 将外部会话句柄解析为内部 Family 标识，并验证反向索引一致。
     *
     * @param sessionId 在线用户管理操作提供的随机会话句柄。
     * @return 返回由安全 Redis 映射出的内部 Family 标识。
     */
    String resolveFamily(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("会话已失效，请刷新列表");
        }
        return SecurityRedisExecutor.execute("解析安全会话句柄", () -> {
            String sessionHandleKey = sessionHandleKey(sessionId);
            Object familyValue = value("解析安全会话句柄", sessionHandleKey);
            if (familyValue == null) {
                throw new IllegalArgumentException("会话已失效，请刷新列表");
            }
            String familyId = SecurityRedisValueParser.requiredText(familyValue, "SessionHandle.familyId");
            Object storedHandle = value("校验安全会话句柄映射", familyHandleKey(familyId));
            if (storedHandle == null
                    || !sessionId.equals(SecurityRedisValueParser.requiredText(storedHandle, "FamilyHandle.sessionId"))) {
                throw new SecurityRedisUnavailableException("安全 Redis 会话句柄索引不一致", null);
            }
            return familyId;
        });
    }

    /** 清理 Family 与随机句柄之间的双向索引。 */
    void deleteFamilyHandle(String familyId) {
        SecurityRedisExecutor.run("清理安全会话句柄", () -> {
            String familyKey = familyHandleKey(familyId);
            Object rawHandle = value("读取待清理安全会话句柄", familyKey);
            if (rawHandle == null) {
                return;
            }
            String handle = SecurityRedisValueParser.requiredText(rawHandle, "FamilyHandle.sessionId");
            String lookupKey = sessionHandleKey(handle);
            Object mappedFamily = value("校验待清理安全会话句柄", lookupKey);
            if (mappedFamily != null
                    && familyId.equals(SecurityRedisValueParser.requiredText(mappedFamily, "SessionHandle.familyId"))) {
                redis.delete(lookupKey);
            }
            redis.delete(familyKey);
        });
    }

    private String createOrGetInternal(String familyId, Duration ttl) {
        String familyKey = familyHandleKey(familyId);
        String sessionId = null;
        for (int attempt = 0; attempt < 2 && sessionId == null; attempt++) {
            String candidate = TokenDigestService.generateToken();
            Boolean claimed = SecurityRedisExecutor.require("创建 Family 会话句柄", () -> redis.opsForValue().setIfAbsent(familyKey, candidate, ttl));
            if (claimed) {
                sessionId = candidate;
            } else {
                Object storedHandle = value("读取 Family 会话句柄", familyKey);
                if (storedHandle != null) {
                    sessionId = SecurityRedisValueParser.requiredText(storedHandle, "FamilyHandle.sessionId");
                }
            }
        }
        if (sessionId == null) {
            throw new SecurityRedisUnavailableException("安全 Redis 未返回 Family 会话句柄", null);
        }

        String lookupKey = sessionHandleKey(sessionId);
        Object mappedFamilyValue = value("读取会话句柄索引", lookupKey);
        String mappedFamily = mappedFamilyValue == null
                ? null
                : SecurityRedisValueParser.requiredText(mappedFamilyValue, "SessionHandle.familyId");
        if (mappedFamily == null) {
            Boolean claimed = SecurityRedisExecutor.require("创建会话句柄索引", () -> redis.opsForValue().setIfAbsent(lookupKey, familyId, ttl));
            if (!claimed) {
                mappedFamily = SecurityRedisValueParser.requiredText(
                        value("校验会话句柄索引", lookupKey), "SessionHandle.familyId");
            } else {
                mappedFamily = familyId;
            }
        }
        if (!familyId.equals(mappedFamily)) {
            throw new SecurityRedisUnavailableException("安全 Redis 会话句柄摘要映射冲突", null);
        }

        Boolean familyExtended = SecurityRedisExecutor.require("延长 Family 会话句柄有效期", () -> redis.expire(familyKey, ttl));
        Boolean handleExtended = SecurityRedisExecutor.require("延长会话句柄索引有效期", () -> redis.expire(lookupKey, ttl));
        if (!familyExtended || !handleExtended) {
            throw new SecurityRedisUnavailableException("安全 Redis 未能延长会话句柄有效期", null);
        }
        return sessionId;
    }

    private Object value(String operation, String key) {
        return SecurityRedisExecutor.execute(operation, () -> redis.opsForValue().get(key));
    }

    private static void requireFamilyAndTtl(String familyId, Duration ttl) {
        if (familyId == null || familyId.isBlank()) {
            throw new IllegalArgumentException("会话 Family 不能为空");
        }
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            throw new IllegalArgumentException("会话句柄有效期必须大于零");
        }
    }

    private static String sessionHandleKey(String sessionId) {
        return SecurityRedisKey.SESSION_HANDLE.format(TokenDigestService.digest(sessionId));
    }

    private static String familyHandleKey(String familyId) {
        return SecurityRedisKey.FAMILY_HANDLE.format(familyId);
    }
}

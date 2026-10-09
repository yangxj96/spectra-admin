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

package com.devops00.spectra.framework.security.redis.store;

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisExecutor;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

/**
 * Refresh Token 一次性消费存储操作。
 *
 * <p>通过 Lua 在 Redis 内原子完成“Refresh Hash 与用户索引有效、消费声明只成功一次；重放立即立 Family 围栏”，避免并发刷新同时成功。
 * 消费声明使用独立 String Key，避免直接修改 Refresh Hash 时受 HashValueSerializer 影响。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026-08-14
 *
 */
public final class RefreshTokenRotationStore {

    private static final RedisScript<Long> CLAIM_SCRIPT = RedisScript.of("""
            local ttl = tonumber(ARGV[2])
            local persistent = false
            for _, index in ipairs({1, 2, 5, 6, 7}) do
              local remaining = redis.call('PTTL', KEYS[index])
              if remaining == -1 then
                persistent = true
              elseif remaining > ttl then
                ttl = remaining
              end
            end
            local function revoke()
              local existing = redis.call('PTTL', KEYS[3])
              if existing == -1 then return end
              if persistent then
                redis.call('SET', KEYS[3], 'REVOKED')
              elseif existing < ttl then
                redis.call('SET', KEYS[3], 'REVOKED', 'PX', ttl)
              end
            end
            if redis.call('EXISTS', KEYS[2]) == 1 then
              revoke()
              return 0
            end
            if redis.call('EXISTS', KEYS[3]) == 1 then return 3 end
            if redis.call('EXISTS', KEYS[1]) == 0 then return -1 end
            if redis.call('SISMEMBER', KEYS[4], ARGV[1]) == 0 then return 2 end
            local claimed
            if persistent then
              claimed = redis.call('SET', KEYS[2], 'CLAIMED', 'NX')
            else
              claimed = redis.call('SET', KEYS[2], 'CLAIMED', 'PX', ttl, 'NX')
            end
            if not claimed then
              revoke()
              return 0
            end
            return 1
            """, Long.class);

    private static final RedisScript<Long> FENCE_SCRIPT = RedisScript.of("""
            local ttl = tonumber(ARGV[1])
            local persistent = false
            for index = 2, #KEYS do
              local remaining = redis.call('PTTL', KEYS[index])
              if remaining == -1 then
                persistent = true
              elseif remaining > ttl then
                ttl = remaining
              end
            end
            local existing = redis.call('PTTL', KEYS[1])
            if existing == -1 then return 1 end
            if persistent then
              redis.call('SET', KEYS[1], 'REVOKED')
            elseif existing < ttl then
              redis.call('SET', KEYS[1], 'REVOKED', 'PX', ttl)
            end
            return 1
            """, Long.class);

    private static final RedisScript<Long> COMPARE_AND_DELETE_SCRIPT = RedisScript.of("""
            local current = redis.call('GET', KEYS[1])
            if current ~= false and current == ARGV[1] then
              redis.call('DEL', KEYS[1])
              return 1
            end
            return 0
            """, Long.class);

    private RefreshTokenRotationStore() {
    }

    /**
     * Refresh Token 消费结果。
     *
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    public enum ClaimResult {
        /** 首次消费成功。 */
        CLAIMED,
        /** 已经消费过，属于重放。 */
        REPLAY,
        /** Redis 中不存在该 Token。 */
        MISSING,
        /** Refresh 不再属于用户活动会话索引。 */
        ORPHAN,
        /** 所属 Family 已被撤销。 */
        FENCED
    }

    /** 首次读取 Refresh Hash 后确认的身份；仅保存摘要与内部标识。 */
    public record ClaimIdentity(String refreshDigest, String accessDigest, String userId, String familyId) {
    }

    /**
     * 原子消费 Refresh Token。
     *
     * @param redis             Redis 模板
     * @param identity          Refresh 与所属会话的摘要和内部标识
     * @param minimumTtlSeconds 围栏及声明至少保留的秒数
     * @return 返回原子消费结果；Redis 异常或未知结果直接 fail-closed。
     */
    public static ClaimResult claim(RedisTemplate<String, Object> redis, ClaimIdentity identity,
                                    long minimumTtlSeconds) {
        List<String> keys = List.of(
                SecurityRedisKey.REFRESH_TOKEN.format(identity.refreshDigest()),
                SecurityRedisKey.REFRESH_CLAIM.format(identity.refreshDigest()),
                SecurityRedisKey.REFRESH_REPLAY_FENCE.format(identity.familyId()),
                SecurityRedisKey.USER_TOKENS.format(identity.userId()),
                SecurityRedisKey.REFRESH_FAMILY.format(identity.familyId()),
                SecurityRedisKey.SESSION_FAMILY.format(identity.familyId()),
                SecurityRedisKey.SESSION.format(identity.accessDigest()));
        Long result = SecurityRedisExecutor.require("消费 Refresh Token",
                () -> redis.execute(CLAIM_SCRIPT, keys, identity.accessDigest(), ttlMillis(minimumTtlSeconds)));
        return switch (result.intValue()) {
            case 1 -> ClaimResult.CLAIMED;
            case 0 -> ClaimResult.REPLAY;
            case -1 -> ClaimResult.MISSING;
            case 2 -> ClaimResult.ORPHAN;
            case 3 -> ClaimResult.FENCED;
            default -> throw new SecurityRedisUnavailableException("安全 Redis 返回未知 Refresh 消费状态", null);
        };
    }

    /** 在清理 Family 前安装只延长的重放围栏；保留期覆盖现存 Family 索引。 */
    public static void markFamilyRevoked(RedisTemplate<String, Object> redis, String familyId,
                                         long minimumTtlSeconds) {
        List<String> keys = List.of(
                SecurityRedisKey.REFRESH_REPLAY_FENCE.format(familyId),
                SecurityRedisKey.REFRESH_FAMILY.format(familyId),
                SecurityRedisKey.SESSION_FAMILY.format(familyId));
        SecurityRedisExecutor.require("安装 Refresh Family 撤销围栏",
                () -> redis.execute(FENCE_SCRIPT, keys, ttlMillis(minimumTtlSeconds)));
    }

    private static long ttlMillis(long seconds) {
        return Math.multiplyExact(seconds, 1000L);
    }

    /**
     * 只有当前值仍与预期值一致时才删除映射，避免并发轮换误删新会话索引。
     *
     * @param redis         安全 Redis 模板
     * @param key           待删除的映射 Key
     * @param expectedValue 调用方提交的刷新令牌摘要；只有与 Redis 中当前摘要一致时才允许删除并完成消费。
     * @return 仅当 Redis 中的值与 expectedValue 匹配并完成原子删除时返回 true；值缺失、不匹配或过期时返回 false，Redis 失败时抛出异常。
     */
    public static boolean compareAndDelete(RedisTemplate<String, Object> redis, String key,
                                           String expectedValue) {
        if (expectedValue == null || expectedValue.isBlank()) {
            return false;
        }
        Long result = SecurityRedisExecutor.require("原子清理安全 Redis 映射",
                () -> redis.execute(COMPARE_AND_DELETE_SCRIPT, List.of(key), expectedValue));
        return Long.valueOf(1L).equals(result);
    }
}

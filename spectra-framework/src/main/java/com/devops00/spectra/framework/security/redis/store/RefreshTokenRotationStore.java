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

import com.devops00.spectra.framework.security.redis.key.SecurityRedisExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

/**
 * Refresh Token 一次性消费存储操作。
 *
 * <p>通过 Lua 在 Redis 内原子完成“Refresh Hash 仍存在且消费声明只成功一次”，避免并发刷新同时成功。
 * 消费声明使用独立 String Key，避免直接修改 Refresh Hash 时受 HashValueSerializer 影响。</p>
 *
 * @author yangxj96
 * @since 2026-08-14
 */
public final class RefreshTokenRotationStore {

    private static final RedisScript<Long> CLAIM_SCRIPT = RedisScript.of("""
            local exists = redis.call('EXISTS', KEYS[1])
            if exists == 0 then
              return -1
            end
            local claimed = redis.call('SETNX', KEYS[2], 'CLAIMED')
            if claimed == 0 then
              return 0
            end
            redis.call('EXPIRE', KEYS[2], ARGV[1])
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
     */
    public enum ClaimResult {
        /** 首次消费成功。 */
        CLAIMED,
        /** 已经消费过，属于重放。 */
        REPLAY,
        /** Redis 中不存在该 Token。 */
        MISSING
    }

    /**
     * 原子消费 Refresh Token。
     *
     * @param redis          Redis 模板
     * @param refreshHashKey Refresh Token Hash Key
     * @param claimKey       一次性消费声明 Key
     * @param ttlSeconds     声明保留时间（秒）
     * @return 消费结果；Redis 返回异常时直接抛出，由上层 fail-closed
     */
    public static ClaimResult claim(RedisTemplate<String, Object> redis, String refreshHashKey, String claimKey,
                                    long ttlSeconds) {
        Long result = SecurityRedisExecutor.require("消费 Refresh Token",
                () -> redis.execute(CLAIM_SCRIPT, List.of(refreshHashKey, claimKey), ttlSeconds));
        return switch (result.intValue()) {
            case 1 -> ClaimResult.CLAIMED;
            case 0 -> ClaimResult.REPLAY;
            default -> ClaimResult.MISSING;
        };
    }

    /**
     * 只有当前值仍与预期值一致时才删除映射，避免并发轮换误删新会话索引。
     *
     * @param redis         安全 Redis 模板
     * @param key           待删除的映射 Key
     * @param expectedValue 期望的当前值
     * @return 是否完成删除
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

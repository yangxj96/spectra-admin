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

import com.devops00.spectra.common.port.security.SecurityVerificationAttemptStore;
import com.devops00.spectra.common.port.security.SecurityVerificationCodeStore;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 安全验证码及其失败尝试计数的 Redis 适配器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
@Component
public class RedisSecurityVerificationStore implements SecurityVerificationCodeStore, SecurityVerificationAttemptStore {

    private static final RedisScript<Long> COMPARE_AND_DELETE_SCRIPT = new DefaultRedisScript<>("""
            local current = redis.call('GET', KEYS[1])
            if current ~= false and current == ARGV[1] then
                redis.call('DEL', KEYS[1])
                return 1
            end
            return 0
            """, Long.class);

    private final RedisTemplate<String, Object> redis;

    public RedisSecurityVerificationStore(
                                          @Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redis) {
        this.redis = redis;
    }

    /**
     * 保存验证码状态，并按照安全策略设置过期时间。
     *
     * @param key   已按安全命名空间生成的 Redis 键。
     * @param value 待保存的验证码或验证码相关安全状态值，不得写入日志。
     * @param ttl   安全 Redis 条目的存活时间，过期后不可继续作为安全状态使用。
     */
    @Override
    public void save(String key, String value, Duration ttl) {
        SecurityRedisExecutor.run("写入验证码", () -> redis.opsForValue().set(key, value, ttl));
    }

    /**
     * 仅在验证码状态不存在时原子写入，避免重复消费。
     *
     * @param key   已按安全命名空间生成的 Redis 键。
     * @param value 待在键不存在时保存的验证码安全状态值，不得写入日志。
     * @param ttl   安全 Redis 条目的存活时间，过期后不可继续作为安全状态使用。
     * @return 首次写入验证码状态时返回 true，键已存在时返回 false；Redis 操作失败时抛出异常，不把失败降级为 false。
     */
    @Override
    public boolean saveIfAbsent(String key, String value, Duration ttl) {
        Boolean stored = SecurityRedisExecutor.require("写入验证码", () -> redis.opsForValue().setIfAbsent(key, value, ttl));
        return Boolean.TRUE.equals(stored);
    }

    /**
     * 仅在值匹配时原子删除验证码状态，防止重放。
     *
     * @param key           已按安全命名空间生成的 Redis 键。
     * @param expectedValue 调用方提交的验证码状态值；只有与 Redis 当前值完全一致时才允许消费。
     * @return 仅当 Redis 中的验证码值与 expectedValue 匹配并完成原子删除时返回 true；值缺失、过期或不匹配时返回 false，Redis 失败时抛出异常。
     */
    @Override
    public boolean compareAndDelete(String key, String expectedValue) {
        if (expectedValue == null || expectedValue.isBlank()) {
            return false;
        }
        Long result = SecurityRedisExecutor.require("消费验证码",
                () -> redis.execute(COMPARE_AND_DELETE_SCRIPT, List.of(key), expectedValue));
        return Long.valueOf(1L).equals(result);
    }

    /**
     * 删除已消费或已撤销的安全 Redis 状态。
     *
     * @param key 已按安全命名空间生成的 Redis 键。
     */
    @Override
    public void delete(String key) {
        SecurityRedisExecutor.run("清理验证码", () -> redis.delete(key));
    }

    /**
     * 原子增加安全 Redis 计数并返回当前计数。
     *
     * @param key 已按安全命名空间生成的 Redis 键。
     * @param ttl 安全 Redis 条目的存活时间，过期后不可继续作为安全状态使用。
     * @return 返回增加后的验证码失败尝试次数；首次计数会设置 TTL，Redis 失败时抛出异常，不返回 null。
     */
    @Override
    public long increment(String key, Duration ttl) {
        Long attempts = SecurityRedisExecutor.require("记录验证码失败次数",
                () -> redis.opsForValue().increment(key));
        if (attempts == 1L) {
            SecurityRedisExecutor.run("设置验证码失败次数 TTL",
                    () -> redis.expire(key, ttl.toSeconds(), TimeUnit.SECONDS));
        }
        return attempts;
    }

}

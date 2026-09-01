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

package com.devops00.spectra.security.starter.ratelimit;

import com.devops00.spectra.security.base.constant.SecurityRedisNamespace;
import com.devops00.spectra.security.base.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.security.base.util.SecurityRedisExecutor;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

/**
 * 基于安全 Redis 的固定窗口限流器。
 *
 * <p>计数由 Redis Lua 脚本在服务端原子完成，不能切换为本地内存计数。
 * Redis 不可用、脚本返回异常或 TTL 不可信时，统一抛出安全 Redis 不可用异常，
 * 由 HTTP 过滤器执行 fail-closed。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/1
 */
@NullMarked
public final class RedisRateLimiter {

    private static final String KEY_VERSION = "v1";

    private static final RedisScript<List> INCREMENT_SCRIPT = new DefaultRedisScript<>("""
            local current = redis.call('INCR', KEYS[1])
            if current == 1 then
                redis.call('PEXPIRE', KEYS[1], ARGV[1])
            end
            local ttl = redis.call('PTTL', KEYS[1])
            return {current, ttl}
            """, List.class);

    private final RateLimitStore store;

    private final Clock clock;

    /**
     * 使用 Spring Redis Template 创建生产限流器。
     *
     * @param redis 安全 Redis Template
     */
    public RedisRateLimiter(RedisTemplate<String, Object> redis) {
        this((key, ttlMillis) -> executeRedisScript(redis, key, ttlMillis), Clock.systemUTC());
    }

    /**
     * 使用可替换存储和时钟创建限流器，供同包行为测试使用。
     */
    RedisRateLimiter(RateLimitStore store, Clock clock) {
        this.store = Objects.requireNonNull(store, "限流存储不能为空");
        this.clock = Objects.requireNonNull(clock, "限流时钟不能为空");
    }

    /**
     * 尝试消费一次限流额度。
     *
     * @param policy  限流策略
     * @param subject 限流主体
     * @return 限流决策
     * @throws SecurityRedisUnavailableException Redis 状态不可用或不可信
     */
    public Decision tryAcquire(RateLimitPolicy policy, RateLimitPolicy.Subject subject) {
        Objects.requireNonNull(policy, "限流策略不能为空");
        Objects.requireNonNull(subject, "限流主体不能为空");

        long windowMillis = policy.window().toMillis();
        if (windowMillis < 1) {
            throw new IllegalArgumentException("限流窗口必须至少为 1 毫秒");
        }
        long window = Math.floorDiv(clock.millis(), windowMillis);
        String key = buildKey(policy, subject, window);
        IncrementResult result = SecurityRedisExecutor.require("执行 API 限流",
                () -> store.increment(key, windowMillis));
        validateResult(result);

        long remaining = Math.max(0, (long) policy.maxRequests() - result.count());
        long retryAfterSeconds = ceilSeconds(result.ttlMillis());
        return new Decision(result.count() <= policy.maxRequests(), remaining, retryAfterSeconds);
    }

    private static IncrementResult executeRedisScript(RedisTemplate<String, Object> redis, String key, long ttlMillis) {
        // Lua 的 PEXPIRE 参数必须是裸数字；String 会被 JSON 序列化为带引号的参数。
        List<?> response = redis.execute(INCREMENT_SCRIPT, List.of(key), ttlMillis);
        if (response == null || response.size() < 2) {
            throw new SecurityRedisUnavailableException("安全 Redis 限流脚本未返回完整结果", null);
        }
        return new IncrementResult(asLong(response.getFirst(), "限流计数"), asLong(response.get(1), "限流 TTL"));
    }

    private static long asLong(Object value, String field) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text) {
            try {
                return Long.parseLong(text);
            } catch (NumberFormatException exception) {
                throw new SecurityRedisUnavailableException("安全 Redis 未返回有效的" + field, exception);
            }
        }
        throw new SecurityRedisUnavailableException("安全 Redis 未返回有效的" + field, null);
    }

    private static void validateResult(IncrementResult result) {
        if (result.count() < 1 || result.ttlMillis() < 1) {
            throw new SecurityRedisUnavailableException("安全 Redis 限流结果不可信", null);
        }
    }

    private static long ceilSeconds(long millis) {
        return Math.max(1, (millis + 999) / 1000);
    }

    private static String buildKey(RateLimitPolicy policy, RateLimitPolicy.Subject subject, long window) {
        String subjectDigest = sha256(subject.key(policy.subjectDimension()));
        return SecurityRedisNamespace.PREFIX + "ratelimit:" + KEY_VERSION + ":" + policy.name() + ":"
                + subjectDigest + ":" + window;
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("JDK 必须提供 SHA-256", exception);
        }
    }

    /** 限流结果。 */
    public record Decision(boolean allowed, long remaining, long retryAfterSeconds) {
    }

    /** Redis 原子计数结果。 */
    record IncrementResult(long count, long ttlMillis) {
    }

    /** Redis 原子计数存储适配器。 */
    @FunctionalInterface
    interface RateLimitStore {

        /**
         * 原子增加窗口计数并返回当前计数和剩余 TTL。
         */
        IncrementResult increment(String key, long ttlMillis);
    }
}

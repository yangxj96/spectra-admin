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

package com.devops00.spectra.framework.security.replay;

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.common.port.security.SecurityReplayNonceAdminPort;
import com.devops00.spectra.common.security.crypto.digest.SHA256Utils;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisExecutor;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;

/**
 * 安全 Redis 中 Web 加密 nonce 的管理实现。
 *
 * <p>该组件只允许访问固定的 crypto nonce 和 cutoff 键，不能被当作通用
 * Redis 清理器使用。全局失效先推进 cutoff，再按记录时间删除旧 nonce；即使
 * 清理阶段失败，已经持久化的 cutoff 仍会让旧请求保持失效。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Component
public class SecurityReplayNonceAdminService implements SecurityReplayNonceAdminPort {

    private static final String AVAILABLE = "AVAILABLE";
    private static final String SUCCEEDED = "SUCCEEDED";
    private static final String TARGET = "TARGET";
    private static final String ALL = "ALL";
    private static final String NONCE_PATTERN = SecurityRedisKey.CRYPTO_NONCE.getPattern().replace("%s", "*");

    private final RedisTemplate<String, Object> redis;
    private final SecurityProperties securityProperties;

    public SecurityReplayNonceAdminService(
                                           @Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redis,
                                           SecurityProperties securityProperties) {
        this.redis = redis;
        this.securityProperties = securityProperties;
    }

    /**
     * 查询 Web 加密 nonce 的脱敏安全运行态。
     *
     * @return 返回安全 Redis 可用状态和当前全局 nonce cutoff；不返回 nonce、Key 或值。
     */
    @Override
    public Summary summary() {
        Long cutoff = readCutoff();
        return new Summary(AVAILABLE, cutoff);
    }

    /**
     * 定向标记一个 Web 加密 nonce 失效。
     *
     * @param nonce 待失效的 nonce 原文；只在内存中计算摘要，不写入日志或响应。
     * @return 返回定向失效的操作类型和首次写入影响数量。
     */
    @Override
    public Result invalidate(String nonce) {
        return invalidateAt(nonce, epochSecond());
    }

    /**
     * 按请求时间戳标记 Web 加密 nonce，供请求解密链路在 cutoff 之后安全消费。
     *
     * @param nonce            待消费的 nonce 原文；只在内存中计算摘要。
     * @param requestTimestamp 请求信封中的时间戳，作为失效记录的时间标记。
     * @return 返回定向失效的操作类型和首次写入影响数量。
     */
    @Override
    public Result invalidate(String nonce, long requestTimestamp) {
        return invalidateAt(nonce, requestTimestamp);
    }

    /**
     * 处理安全随机数相关数据。
     */
    private Result invalidateAt(String nonce, long markerTimestamp) {
        validateNonce(nonce);
        String nonceKey = SecurityRedisKey.CRYPTO_NONCE.format(SHA256Utils.hash(nonce));
        Boolean stored = SecurityRedisExecutor.require("记录定向 nonce 失效",
                () -> redis.opsForValue().setIfAbsent(nonceKey, markerTimestamp, nonceTtl()));
        return new Result(TARGET, Boolean.TRUE.equals(stored) ? 1L : 0L, null, SUCCEEDED);
    }

    /**
     * 推进全局 Web 加密 nonce 失效窗口，并清理窗口之前的记录。
     *
     * @return 返回全局操作类型、清理数量和已写入的 cutoff 时间。
     */
    @Override
    public Result invalidateAll() {
        long cutoff = epochSecond();
        SecurityRedisExecutor.run("写入 nonce 全局失效 cutoff",
                () -> redis.opsForValue().set(SecurityRedisKey.CRYPTO_NONCE_CUTOFF.getPattern(), cutoff, nonceTtl()));
        long deleted = deleteNonceRecordsAtOrBefore(cutoff);
        return new Result(ALL, deleted, cutoff, SUCCEEDED);
    }

    /**
     * 判断请求时间戳是否落入当前全局 nonce 失效窗口。
     *
     * @param timestamp 请求加密信封时间戳。
     * @return cutoff 存在且请求时间戳不晚于 cutoff 时返回 true，否则返回 false。
     */
    @Override
    public boolean isBeforeOrAtCutoff(long timestamp) {
        Long cutoff = readCutoff();
        return cutoff != null && timestamp <= cutoff;
    }

    /**
     * 查询安全随机数。
     */
    private Long readCutoff() {
        Object value = SecurityRedisExecutor.execute("读取 nonce 全局失效 cutoff",
                () -> redis.opsForValue().get(SecurityRedisKey.CRYPTO_NONCE_CUTOFF.getPattern()));
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException exception) {
            throw new SecurityRedisUnavailableException("安全 Redis nonce cutoff 状态无效", exception);
        }
    }

    /**
     * 删除或清理随机数记录集合。
     */
    private long deleteNonceRecordsAtOrBefore(long cutoff) {
        Long deleted = SecurityRedisExecutor.require("扫描并清理旧 nonce",
                () -> redis.execute((RedisCallback<Long>) connection -> scanAndDelete(connection, cutoff)));
        return deleted;
    }

    /**
     * 处理删除相关数据。
     */
    private long scanAndDelete(RedisConnection connection, long cutoff) {
        RedisSerializer<?> keySerializer = redis.getKeySerializer();
        RedisSerializer<?> valueSerializer = redis.getValueSerializer();
        byte[] pattern = serialize(keySerializer, NONCE_PATTERN);
        byte[] cutoffKey = serialize(keySerializer, SecurityRedisKey.CRYPTO_NONCE_CUTOFF.getPattern());
        long deleted = 0L;
        try (var cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(100).build())) {
            while (cursor.hasNext()) {
                byte[] key = cursor.next();
                if (Arrays.equals(key, cutoffKey)) {
                    continue;
                }
                byte[] rawValue = connection.get(key);
                Long createdAt = deserializeEpoch(valueSerializer, rawValue);
                if (createdAt != null && createdAt <= cutoff) {
                    Long result = connection.del(key);
                    if (result != null) {
                        deleted += result;
                    }
                }
            }
        } catch (Exception exception) {
            throw new SecurityRedisUnavailableException("安全 Redis nonce 清理状态不可确认", exception);
        }
        return deleted;
    }

    /**
     * 处理安全随机数相关数据。
     */
    private static byte[] serialize(RedisSerializer<?> serializer, String value) {
        @SuppressWarnings("unchecked")
        RedisSerializer<Object> typedSerializer = (RedisSerializer<Object>) serializer;
        byte[] result = typedSerializer.serialize(value);
        if (result == null) {
            throw new SecurityRedisUnavailableException("安全 Redis nonce 扫描模式不可序列化", null);
        }
        return result;
    }

    /**
     * 处理安全随机数相关数据。
     */
    private static Long deserializeEpoch(RedisSerializer<?> serializer, byte[] rawValue) {
        if (rawValue == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        RedisSerializer<Object> typedSerializer = (RedisSerializer<Object>) serializer;
        Object value = typedSerializer.deserialize(rawValue);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException exception) {
            throw new SecurityRedisUnavailableException("安全 Redis nonce 记录格式无效", exception);
        }
    }

    /**
     * 处理随机数相关数据。
     */
    private Duration nonceTtl() {
        return Duration.ofSeconds(securityProperties.getCryptoReplayWindowSeconds());
    }

    /**
     * 处理安全随机数相关数据。
     */
    private static long epochSecond() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 校验随机数。
     */
    private static void validateNonce(String nonce) {
        if (nonce == null
                || nonce.isBlank()
                || nonce.length() > 128
                || !nonce.matches("[A-Za-z0-9._:-]+")) {
            throw new IllegalArgumentException("nonce 格式无效");
        }
    }
}

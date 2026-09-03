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

package com.devops00.spectra.framework.configure.security.redis;

import com.devops00.spectra.common.port.security.SecurityVerificationAttemptStore;
import com.devops00.spectra.common.port.security.SecurityVerificationCodeStore;
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

    @Override
    public void save(String key, String value, Duration ttl) {
        SecurityRedisExecutor.run("写入验证码", () -> redis.opsForValue().set(key, value, ttl));
    }

    @Override
    public boolean saveIfAbsent(String key, String value, Duration ttl) {
        Boolean stored = SecurityRedisExecutor.require("写入验证码", () -> redis.opsForValue().setIfAbsent(key, value, ttl));
        return Boolean.TRUE.equals(stored);
    }

    @Override
    public boolean compareAndDelete(String key, String expectedValue) {
        if (expectedValue == null || expectedValue.isBlank()) {
            return false;
        }
        Long result = SecurityRedisExecutor.require("消费验证码",
                () -> redis.execute(COMPARE_AND_DELETE_SCRIPT, List.of(key), expectedValue));
        return Long.valueOf(1L).equals(result);
    }

    @Override
    public void delete(String key) {
        SecurityRedisExecutor.run("清理验证码", () -> redis.delete(key));
    }

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

/*
 * Copyright 2018-2026 yangxj96
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.devops00.spectra.framework.security.redis.store;

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisExecutor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;

/**
 * 保证首次失败计数与有效期同时写入，拒绝无法确认窗口的既有计数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/07
 */
public final class SecurityRedisCounter {

    private static final RedisScript<Long> READ = new DefaultRedisScript<>("""
            local current = redis.call('GET', KEYS[1])
            if not current then return 0 end
            local count = tonumber(current)
            if not count or count < 0 or count ~= math.floor(count) or redis.call('PTTL', KEYS[1]) <= 0 then
                return redis.error_reply('unknown security counter state')
            end
            return count
            """, Long.class);

    private static final RedisScript<Long> INCREMENT = new DefaultRedisScript<>("""
            local ttl = tonumber(ARGV[1])
            if not ttl or ttl < 1 or ttl ~= math.floor(ttl) then
                return redis.error_reply('invalid security counter ttl')
            end
            local current = redis.call('GET', KEYS[1])
            if not current then
                redis.call('SET', KEYS[1], '1', 'PX', ttl)
                return 1
            end
            local count = tonumber(current)
            if not count or count < 0 or count ~= math.floor(count) or redis.call('PTTL', KEYS[1]) <= 0 then
                return redis.error_reply('unknown security counter state')
            end
            return redis.call('INCR', KEYS[1])
            """, Long.class);

    private SecurityRedisCounter() {
    }

    /** 同时确认计数与窗口；健康未命中返回零，缺少有效期时拒绝。 */
    public static long current(RedisTemplate<String, Object> redis, String key) {
        long count = SecurityRedisExecutor.require("读取安全失败计数窗口",
                () -> redis.execute(READ, List.of(key)));
        if (count < 0) {
            throw new SecurityRedisUnavailableException("安全 Redis 返回无效的失败计数", null);
        }
        return count;
    }

    /** 原子累加；首写即带有效期，后续递增保持原来的窗口。 */
    public static long increment(RedisTemplate<String, Object> redis, String key, Duration ttl) {
        if (ttl == null || ttl.isNegative() || ttl.isZero() || ttl.toMillis() < 1) {
            throw new IllegalArgumentException("安全计数有效期必须为正毫秒数");
        }
        long count = SecurityRedisExecutor.require("原子记录安全失败次数",
                () -> redis.execute(INCREMENT, List.of(key), ttl.toMillis()));
        if (count < 1) {
            throw new SecurityRedisUnavailableException("安全 Redis 返回无效的失败计数", null);
        }
        return count;
    }
}

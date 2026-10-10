/*
 * Copyright 2018-2026 yangxj96
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.devops00.spectra.framework.security.session;

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.common.security.policy.SessionConcurrencyMode;
import com.devops00.spectra.common.security.policy.SessionPolicy;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisExecutor;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;

/**
 * 在 Redis 中原子预留和提交用户会话名额，供全部客户端和应用实例共享。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/10
 */
final class SecuritySessionAdmissionStore {

    private static final long LEASE_MILLIS = Duration.ofMinutes(2).toMillis();

    private static final RedisScript<Long> RESERVE = RedisScript.of("""
            local time = redis.call('TIME')
            local now = time[1] * 1000 + math.floor(time[2] / 1000)
            redis.call('ZREMRANGEBYSCORE', KEYS[2], '-inf', now)
            redis.call('ZREMRANGEBYSCORE', KEYS[4], '-inf', now)
            if tonumber(ARGV[2]) == 2 and
                redis.call('SCARD', KEYS[1]) + redis.call('ZCARD', KEYS[2]) >= tonumber(ARGV[3]) then
              return 0
            end
            if tonumber(ARGV[2]) == 1 then
              if redis.call('ZCARD', KEYS[4]) > 0 or
                  not redis.call('SET', KEYS[3], ARGV[1], 'NX', 'PX', tonumber(ARGV[4])) then
                return -2
              end
            elseif redis.call('EXISTS', KEYS[3]) == 1 then
              return -2
            end
            redis.call('ZADD', KEYS[2], now + tonumber(ARGV[4]), ARGV[1])
            redis.call('ZADD', KEYS[4], now + tonumber(ARGV[4]), ARGV[1])
            redis.call('PEXPIRE', KEYS[2], tonumber(ARGV[4]))
            redis.call('PEXPIRE', KEYS[4], tonumber(ARGV[4]))
            return 1
            """, Long.class);

    private static final RedisScript<Long> COMMIT = RedisScript.of("""
            local time = redis.call('TIME')
            local now = time[1] * 1000 + math.floor(time[2] / 1000)
            redis.call('ZREMRANGEBYSCORE', KEYS[2], '-inf', now)
            redis.call('ZREMRANGEBYSCORE', KEYS[6], '-inf', now)
            local expiry = redis.call('ZSCORE', KEYS[2], ARGV[1])
            if not expiry or tonumber(expiry) <= now then return 0 end
            if redis.call('ZSCORE', KEYS[6], ARGV[1]) ~= expiry then return 0 end
            if redis.call('EXISTS', KEYS[4]) == 1 then return 0 end
            if tonumber(ARGV[2]) == 1 and redis.call('GET', KEYS[3]) ~= ARGV[1] then return 0 end
            local indexTtl = redis.call('PTTL', KEYS[1])
            if indexTtl == -1 then return -1 end
            if tonumber(ARGV[2]) == 2 and
                redis.call('SCARD', KEYS[1]) + redis.call('ZCARD', KEYS[2]) > tonumber(ARGV[5]) then
              return -3
            end
            redis.call('SADD', KEYS[1], ARGV[1])
            if indexTtl < tonumber(ARGV[3]) then redis.call('PEXPIRE', KEYS[1], tonumber(ARGV[3])) end
            redis.call('SADD', KEYS[5], ARGV[4])
            redis.call('ZREM', KEYS[2], ARGV[1])
            redis.call('ZREM', KEYS[6], ARGV[1])
            if tonumber(ARGV[2]) == 1 then redis.call('DEL', KEYS[3]) end
            return 1
            """, Long.class);

    private static final RedisScript<Long> RELEASE = RedisScript.of("""
            redis.call('ZREM', KEYS[1], ARGV[1])
            redis.call('ZREM', KEYS[3], ARGV[1])
            if redis.call('GET', KEYS[2]) == ARGV[1] then
              redis.call('DEL', KEYS[2])
            end
            return 1
            """, Long.class);

    private final RedisTemplate<String, Object> redis;

    SecuritySessionAdmissionStore(RedisTemplate<String, Object> redis) {
        this.redis = redis;
    }

    void reserve(String userId, String clientCode, String digest, SessionPolicy policy) {
        Long result = SecurityRedisExecutor.require("预留安全会话名额", () -> redis.execute(RESERVE,
                keys(userId, clientCode), digest, modeCode(policy.concurrencyMode()), policy.maxSessions(), LEASE_MILLIS));
        if (result == 1L) {
            return;
        }
        if (result == 0L) {
            throw new IllegalStateException("已达到该账号的最大并发会话数");
        }
        if (result == -2L) {
            throw new IllegalStateException("该客户端正在创建会话，请稍后重试");
        }
        throw new SecurityRedisUnavailableException("安全 Redis 返回未知会话名额预留状态", null);
    }

    void commit(String userId, String clientCode, String digest, String familyId, SessionPolicy policy) {
        Long result = SecurityRedisExecutor.require("提交安全会话名额", () -> redis.execute(COMMIT,
                List.of(SecurityRedisKey.USER_TOKENS.format(userId),
                        SecurityRedisKey.SESSION_ISSUE_RESERVATIONS.format(userId),
                        SecurityRedisKey.SESSION_ISSUE_CLIENT_LOCK.format(userId, clientCode),
                        SecurityRedisKey.REFRESH_REPLAY_FENCE.format(familyId),
                        SecurityRedisKey.ONLINE_USERS.getPattern(),
                        SecurityRedisKey.SESSION_ISSUE_CLIENT_RESERVATIONS.format(userId, clientCode)),
                digest, modeCode(policy.concurrencyMode()),
                Math.multiplyExact(policy.refreshTtlSeconds(), 1000L), userId, policy.maxSessions()));
        if (result == -3L) {
            throw new IllegalStateException("已达到该账号的最大并发会话数");
        }
        if (result != 1L) {
            throw new SecurityRedisUnavailableException("安全 Redis 会话名额租约失效或索引状态无效", null);
        }
    }

    void release(String userId, String clientCode, String digest, SessionConcurrencyMode mode) {
        SecurityRedisExecutor.require("释放安全会话名额", () -> redis.execute(RELEASE,
                List.of(SecurityRedisKey.SESSION_ISSUE_RESERVATIONS.format(userId),
                        SecurityRedisKey.SESSION_ISSUE_CLIENT_LOCK.format(userId, clientCode),
                        SecurityRedisKey.SESSION_ISSUE_CLIENT_RESERVATIONS.format(userId, clientCode)),
                digest, modeCode(mode)));
    }

    private static List<String> keys(String userId, String clientCode) {
        return List.of(SecurityRedisKey.USER_TOKENS.format(userId),
                SecurityRedisKey.SESSION_ISSUE_RESERVATIONS.format(userId),
                SecurityRedisKey.SESSION_ISSUE_CLIENT_LOCK.format(userId, clientCode),
                SecurityRedisKey.SESSION_ISSUE_CLIENT_RESERVATIONS.format(userId, clientCode));
    }

    private static int modeCode(SessionConcurrencyMode mode) {
        return switch (mode) {
            case ALLOW -> 0;
            case KICK_OLD -> 1;
            case REJECT_NEW -> 2;
        };
    }
}

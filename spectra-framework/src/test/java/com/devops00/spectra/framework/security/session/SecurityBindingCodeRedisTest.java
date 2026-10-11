/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.devops00.spectra.framework.security.session;

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.common.port.security.SecurityVerificationCodeStore.VerificationResult;
import com.devops00.spectra.framework.security.redis.store.RedisSecurityVerificationStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 使用专用 Redis DB 5 验证绑定码的跨连接原子尝试预算。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/11
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class SecurityBindingCodeRedisTest {
    private LettuceConnectionFactory firstFactory;
    private LettuceConnectionFactory secondFactory;
    private RedisTemplate<String, Object> firstRedis;
    private RedisSecurityVerificationStore first;
    private RedisSecurityVerificationStore second;
    private String keyPrefix;

    @BeforeEach
    void setUp() {
        var configuration = RealRedisTestEnvironment.configuration();
        firstFactory = new LettuceConnectionFactory(configuration);
        secondFactory = new LettuceConnectionFactory(configuration);
        firstFactory.afterPropertiesSet();
        secondFactory.afterPropertiesSet();
        firstFactory.start();
        secondFactory.start();
        keyPrefix = RealRedisTestEnvironment.newKeyPrefix();
        firstRedis = RealRedisTestEnvironment.template(firstFactory, keyPrefix);
        first = new RedisSecurityVerificationStore(firstRedis);
        second = new RedisSecurityVerificationStore(RealRedisTestEnvironment.template(secondFactory, keyPrefix));
    }

    @AfterEach
    void close() {
        try {
            if (firstFactory != null && keyPrefix != null) {
                RealRedisTestEnvironment.clearOwnKeys(firstFactory, keyPrefix);
            }
        } finally {
            if (secondFactory != null) {
                secondFactory.destroy();
            }
            if (firstFactory != null) {
                firstFactory.destroy();
            }
        }
    }

    @Test
    void anotherUserCannotConsumeOwnersCodeAndReplayIsRejected() {
        var owner = UUID.randomUUID();
        var key = "bind:phone:13800000001";
        var attempts = key + ":attempts";
        var codeDigest = "synthetic-digest";
        assertTrue(first.saveIfAbsent(key, owner + ":" + codeDigest, Duration.ofMinutes(2)));

        for (int index = 0; index < 8; index++) {
            assertEquals(VerificationResult.INVALID, second.consumeWithinAttemptBudget(key, attempts,
                    UUID.randomUUID() + ":" + codeDigest, 5));
        }
        assertFalse(Boolean.TRUE.equals(firstRedis.hasKey(attempts)));
        assertEquals(VerificationResult.MATCHED, first.consumeWithinAttemptBudget(key, attempts,
                owner + ":" + codeDigest, 5));
        assertEquals(VerificationResult.INVALID, second.consumeWithinAttemptBudget(key, attempts,
                owner + ":" + codeDigest, 5));
        assertFalse(Boolean.TRUE.equals(firstRedis.hasKey(attempts)));
    }

    @Test
    void parallelGuessesAcrossTwoConnectionsExhaustOneCodeAtomically() throws Exception {
        var key = "bind:email:user@example.test";
        var attempts = key + ":attempts";
        first.saveIfAbsent(key, "owner:correct", Duration.ofMinutes(2));
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var start = new CountDownLatch(1);
            var tasks = new ArrayList<Future<VerificationResult>>();
            for (int index = 0; index < 32; index++) {
                var store = index % 2 == 0 ? first : second;
                tasks.add(executor.submit(() -> {
                    start.await();
                    return store.consumeWithinAttemptBudget(key, attempts, "owner:wrong", 5);
                }));
            }
            start.countDown();
            var outcomes = new ArrayList<VerificationResult>();
            for (var task : tasks) {
                outcomes.add(task.get(10, TimeUnit.SECONDS));
            }
            assertEquals(1, outcomes.stream().filter(VerificationResult.EXHAUSTED::equals).count());
            assertEquals(31, outcomes.stream().filter(VerificationResult.INVALID::equals).count());
        }
        assertFalse(Boolean.TRUE.equals(firstRedis.hasKey(key)));
        assertFalse(Boolean.TRUE.equals(firstRedis.hasKey(attempts)));
        assertEquals(VerificationResult.INVALID, first.consumeWithinAttemptBudget(key, attempts, "owner:correct", 5));
        assertTrue(second.saveIfAbsent(key, "owner:new", Duration.ofMinutes(2)));
        assertEquals(VerificationResult.MATCHED, first.consumeWithinAttemptBudget(key, attempts, "owner:new", 5));
    }

    @Test
    void missingExpiryAndRedisFailureRejectWithoutTreatingCodeAsAbsent() {
        var key = "bind:phone:unknown-expiry";
        var attempts = key + ":attempts";
        firstRedis.opsForValue().set(key, "owner:digest");
        assertThrows(SecurityRedisUnavailableException.class,
                () -> first.consumeWithinAttemptBudget(key, attempts, "owner:digest", 5));
        assertTrue(Boolean.TRUE.equals(firstRedis.hasKey(key)));
        first.delete(key);
        firstRedis.opsForValue().set(key, "malformed-owner", Duration.ofMinutes(2));
        assertThrows(SecurityRedisUnavailableException.class,
                () -> first.consumeWithinAttemptBudget(key, attempts, "owner:digest", 5));
        assertTrue(Boolean.TRUE.equals(firstRedis.hasKey(key)));
        first.delete(key);

        var interrupted = new RedisSecurityVerificationStore(new RedisTemplate<>() {
            @Override
            public <T> T execute(RedisScript<T> script, List<String> keys, Object... args) {
                throw new QueryTimeoutException("synthetic Redis command failure");
            }
        });
        assertThrows(SecurityRedisUnavailableException.class,
                () -> interrupted.consumeWithinAttemptBudget(key, attempts, "owner:digest", 5));
        assertTrue(first.saveIfAbsent(key, "owner:digest", Duration.ofMinutes(2)));
        assertEquals(VerificationResult.MATCHED,
                second.consumeWithinAttemptBudget(key, attempts, "owner:digest", 5));
    }

    @Test
    void lostReplyAfterConsumptionFailsClosedAndCannotReplay() {
        var key = "bind:email:lost-reply@example.test";
        var attempts = key + ":attempts";
        first.saveIfAbsent(key, "owner:digest", Duration.ofMinutes(2));
        var interrupted = new RedisSecurityVerificationStore(new RedisTemplate<>() {
            @Override
            public <T> T execute(RedisScript<T> script, List<String> keys, Object... args) {
                firstRedis.execute(script, keys, args);
                throw new QueryTimeoutException("synthetic lost Redis reply");
            }
        });

        assertThrows(SecurityRedisUnavailableException.class,
                () -> interrupted.consumeWithinAttemptBudget(key, attempts, "owner:digest", 5));
        assertEquals(VerificationResult.INVALID,
                second.consumeWithinAttemptBudget(key, attempts, "owner:digest", 5));
        assertFalse(Boolean.TRUE.equals(firstRedis.hasKey(key)));
    }
}

package com.devops00.spectra.framework.security.session;

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.common.security.policy.SecuritySessionPolicyProvider;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.store.RedisSecurityVerificationStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 使用 test 环境的 Redis DB 5 验证计数及 TTL 的原子边界。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/07
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class SecurityCounterRedisTest {
    private LettuceConnectionFactory factory;
    private RedisTemplate<String, Object> redis;
    private RedisSecurityVerificationStore verification;
    private SecurityLoginFailureStore login;
    private SecurityProperties properties;
    private String keyPrefix;

    @BeforeEach
    void setUp() {
        var config = RealRedisTestEnvironment.configuration();
        factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();
        factory.start();
        properties = new SecurityProperties();
        keyPrefix = RealRedisTestEnvironment.newKeyPrefix();
        redis = RealRedisTestEnvironment.template(factory, keyPrefix);
        verification = new RedisSecurityVerificationStore(redis);
        login = new SecurityLoginFailureStore(new SecuritySessionStore(redis, properties,
                new StaticListableBeanFactory().getBeanProvider(SecuritySessionPolicyProvider.class)));
    }

    @AfterEach
    void close() {
        if (factory == null) {
            return;
        }
        try {
            if (keyPrefix != null) {
                RealRedisTestEnvironment.clearOwnKeys(factory, keyPrefix);
            }
        } finally {
            factory.destroy();
        }
    }

    @Test
    void countersWithoutExpiryAreUnknownSecurityState() {
        redis.opsForValue().set("attempts", 3);
        assertEquals(-1L, redis.getExpire("attempts", TimeUnit.MILLISECONDS));
        assertThrows(SecurityRedisUnavailableException.class,
                () -> verification.increment("attempts", Duration.ofMinutes(1)));
        assertEquals(3, ((Number) redis.opsForValue().get("attempts")).intValue());
        var key = SecurityRedisKey.LOGIN_FAIL.format("synthetic-identity");
        redis.opsForValue().set(key, 3);
        assertThrows(SecurityRedisUnavailableException.class, () -> login.recordLoginFail("synthetic-identity"));
        assertEquals(3, ((Number) redis.opsForValue().get(key)).intValue());
        assertEquals(-1L, redis.getExpire(key, TimeUnit.MILLISECONDS));

        verification.delete("attempts");
        assertEquals(1L, verification.increment("attempts", Duration.ofMinutes(1)));
        assertTrue(redis.getExpire("attempts", TimeUnit.MILLISECONDS) > 0);
        login.clearLoginFail("synthetic-identity");
        login.recordLoginFail("synthetic-identity");
        assertTrue(redis.getExpire(key, TimeUnit.MILLISECONDS) > 0);
    }

    @Test
    void interruptedBeforeFirstWriteRejectsAndRecoveryCreatesExpiringCounter() {
        var interrupted = new RedisSecurityVerificationStore(interruptedRedis(false));
        assertThrows(SecurityRedisUnavailableException.class,
                () -> interrupted.increment("interrupted-before-write", Duration.ofMinutes(1)));
        assertFalse(Boolean.TRUE.equals(redis.hasKey("interrupted-before-write")));

        assertEquals(1L, verification.increment("interrupted-before-write", Duration.ofMinutes(1)));
        assertTrue(redis.getExpire("interrupted-before-write", TimeUnit.MILLISECONDS) > 0);
    }

    @Test
    void lostFirstWriteReplyRejectsButKeepsCounterAndOriginalExpiry() {
        var interrupted = new SecurityLoginFailureStore(new SecuritySessionStore(interruptedRedis(true), properties,
                new StaticListableBeanFactory().getBeanProvider(SecuritySessionPolicyProvider.class)));
        var key = SecurityRedisKey.LOGIN_FAIL.format("lost-reply");
        assertThrows(SecurityRedisUnavailableException.class, () -> interrupted.recordLoginFail("lost-reply"));
        assertEquals(1, ((Number) redis.opsForValue().get(key)).intValue());
        long initialTtl = redis.getExpire(key, TimeUnit.MILLISECONDS);
        assertTrue(initialTtl > 0);

        login.recordLoginFail("lost-reply");
        assertEquals(2, ((Number) redis.opsForValue().get(key)).intValue());
        long followingTtl = redis.getExpire(key, TimeUnit.MILLISECONDS);
        assertTrue(followingTtl > 0 && followingTtl <= initialTtl);
    }

    @Test
    void lockReadRejectsCounterWhoseWindowCannotBeConfirmed() {
        redis.opsForValue().set(SecurityRedisKey.LOGIN_FAIL.format("unknown-window"), 1);
        assertThrows(SecurityRedisUnavailableException.class, () -> login.isLockedOut("unknown-window"));
    }

    @Test
    void concurrentFirstAttemptsHaveOneExpiryAndNoLostIncrements() throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var start = new CountDownLatch(1);
            var tasks = new ArrayList<Future<Long>>();
            for (int index = 0; index < 32; index++) {
                tasks.add(executor.submit(() -> {
                    start.await();
                    return verification.increment("race", Duration.ofMinutes(1));
                }));
            }
            start.countDown();
            var values = new HashSet<Long>();
            for (var task : tasks) {
                values.add(task.get(10, TimeUnit.SECONDS));
            }
            assertEquals(32, values.size());
            assertTrue(values.contains(1L));
            assertTrue(values.contains(32L));
        }
        assertTrue(redis.getExpire("race", TimeUnit.MILLISECONDS) > 0);
        verification.increment("race", Duration.ofMinutes(10));
        assertTrue(redis.getExpire("race", TimeUnit.MILLISECONDS) <= 60_000);
    }

    @Test
    void lockThresholdAndSuccessfulCleanupUseExpiringCounter() {
        for (int count = 0; count < properties.getLockoutMaxAttempts() - 1; count++) {
            login.recordLoginFail("synthetic-identity");
        }
        assertFalse(login.isLockedOut("synthetic-identity"));
        login.recordLoginFail("synthetic-identity");
        assertTrue(login.isLockedOut("synthetic-identity"));
        assertTrue(redis.getExpire(SecurityRedisKey.LOGIN_FAIL.format("synthetic-identity")) > 0);
        login.clearLoginFail("synthetic-identity");
        assertFalse(login.isLockedOut("synthetic-identity"));
    }

    @Test
    void disabledLockoutDoesNotCreatePermanentCounters() {
        properties.setLockoutSeconds(0);
        login.recordLoginFail("disabled");
        assertFalse(redis.hasKey(SecurityRedisKey.LOGIN_FAIL.format("disabled")));
    }

    @Test
    void malformedCounterFailsClosedWithoutOverwritingIt() {
        redis.opsForValue().set("corrupt", "malformed", Duration.ofMinutes(1));
        assertThrows(SecurityRedisUnavailableException.class,
                () -> verification.increment("corrupt", Duration.ofMinutes(1)));
        assertEquals("malformed", redis.opsForValue().get("corrupt"));
    }

    private RedisTemplate<String, Object> interruptedRedis(boolean commitBeforeInterruption) {
        return new RedisTemplate<>() {
            @Override
            public <T> T execute(RedisScript<T> script, List<String> keys, Object... args) {
                if (commitBeforeInterruption) {
                    redis.execute(script, keys, args);
                }
                throw new QueryTimeoutException("synthetic counter command interruption");
            }
        };
    }
}

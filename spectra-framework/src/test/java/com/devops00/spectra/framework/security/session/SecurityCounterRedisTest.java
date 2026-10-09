package com.devops00.spectra.framework.security.session;

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.common.security.policy.SecuritySessionPolicyProvider;
import com.devops00.spectra.framework.security.configuration.redis.SecJacksonConfiguration;
import com.devops00.spectra.framework.security.configuration.redis.SecRedisConfiguration;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.store.RedisSecurityVerificationStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** 只连接独立端口的合成 Redis，验证计数及 TTL 的原子边界。 */
@EnabledIfSystemProperty(named = "spectra.test.redis.port", matches = "26379")
class SecurityCounterRedisTest {
    private LettuceConnectionFactory factory;
    private RedisTemplate<String, Object> redis;
    private RedisSecurityVerificationStore verification;
    private SecurityLoginFailureStore login;
    private SecurityProperties properties;

    @BeforeEach
    void setUp() {
        var config = new RedisStandaloneConfiguration("127.0.0.1", 26379);
        config.setDatabase(12);
        factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();
        factory.start();
        properties = new SecurityProperties();
        redis = new SecRedisConfiguration().redisTemplate(factory,
                new SecJacksonConfiguration().redisObjectMapper(), properties);
        verification = new RedisSecurityVerificationStore(redis);
        login = new SecurityLoginFailureStore(new SecuritySessionStore(redis, properties,
                new StaticListableBeanFactory().getBeanProvider(SecuritySessionPolicyProvider.class)));
    }

    @AfterEach
    void close() {
        try (var connection = factory.getConnection()) {
            connection.serverCommands().flushDb();
        }
        factory.destroy();
    }

    @Test
    void countersWithoutExpiryAreUnknownSecurityState() {
        redis.opsForValue().set("attempts", 3);
        assertThrows(SecurityRedisUnavailableException.class,
                () -> verification.increment("attempts", Duration.ofMinutes(1)));
        assertEquals(3, ((Number) redis.opsForValue().get("attempts")).intValue());
        var key = SecurityRedisKey.LOGIN_FAIL.format("synthetic-identity");
        redis.opsForValue().set(key, 3);
        assertThrows(SecurityRedisUnavailableException.class, () -> login.recordLoginFail("synthetic-identity"));
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
}

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
package com.devops00.spectra.core.security.authentication.service.impl;

import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.common.port.security.SecurityAuthenticationPort;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.security.policy.SecuritySessionPolicyProvider;
import com.devops00.spectra.core.audit.AuditRecordFactory;
import com.devops00.spectra.core.security.authentication.exception.LoginException;
import com.devops00.spectra.core.security.authentication.constant.LoginType;
import com.devops00.spectra.core.security.authentication.identity.AuthenticationIdentifierHash;
import com.devops00.spectra.core.security.authentication.javabean.entity.SecurityUser;
import com.devops00.spectra.core.security.authentication.javabean.from.LoginFrom;
import com.devops00.spectra.core.system.javabean.from.SecurityLoginFailureClearFrom;
import com.devops00.spectra.core.system.service.impl.CacheManagementServiceImpl;
import com.devops00.spectra.framework.security.configuration.redis.SecJacksonConfiguration;
import com.devops00.spectra.framework.security.configuration.redis.SecRedisConfiguration;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.session.SecurityLoginFailureStore;
import com.devops00.spectra.framework.security.session.SecuritySessionStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 使用 test Redis DB 5 验证两个独立连接上的登录身份锁定、运维清理和成功清理。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/11
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class CoreLoginFailureRedisTest {

    private LettuceConnectionFactory firstFactory;
    private LettuceConnectionFactory secondFactory;
    private RedisTemplate<String, Object> firstRedis;
    private SecurityLoginFailureStore firstStore;
    private SecurityLoginFailureStore secondStore;
    private SecurityProperties properties;
    private String keyPrefix;

    @BeforeEach
    void setUp() {
        var configuration = testRedisConfiguration();
        keyPrefix = "spectra:test:core020:" + UUID.randomUUID() + ":";
        firstFactory = connection(configuration);
        secondFactory = connection(configuration);
        properties = new SecurityProperties();
        firstRedis = template(firstFactory);
        firstStore = tracker(firstRedis);
        secondStore = tracker(template(secondFactory));
    }

    @AfterEach
    void close() {
        SecurityContextHolder.clearContext();
        try {
            if (firstFactory != null && keyPrefix != null) {
                clearOwnKeys();
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
    void variantsCompeteForOneBucketAcrossConnectionsAndBothClearPaths() throws Exception {
        var first = login(firstStore);
        var second = login(secondStore);
        String canonical = "person@example.test";
        String bucket = AuthenticationIdentifierHash.digest(canonical);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {
            var firstAttempt = executor.submit(() -> failedAttempt(first.service(), " PERSON@Example.Test ", ready, start));
            var secondAttempt = executor.submit(() -> failedAttempt(second.service(), canonical, ready, start));
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            firstAttempt.get(10, TimeUnit.SECONDS);
            secondAttempt.get(10, TimeUnit.SECONDS);
        }
        assertFalse(firstStore.isLockedOut(bucket));
        for (int attempt = 2; attempt < properties.getLockoutMaxAttempts(); attempt++) {
            var service = attempt % 2 == 0 ? first.service() : second.service();
            assertThrows(LoginException.class, () -> service.login(request(" Person@EXAMPLE.TEST "), ClientType.WEB));
        }
        assertTrue(firstStore.isLockedOut(bucket));
        assertTrue(secondStore.isLockedOut(bucket));
        assertEquals("账号已锁定，请稍后再试", assertThrows(LoginException.class,
                () -> first.service().login(request(canonical), ClientType.WEB)).getMessage());

        var admin = new CacheManagementServiceImpl(null, null, null, null, null, null, null, secondStore, null);
        assertEquals("SUCCEEDED", admin.clearLoginFailure(new SecurityLoginFailureClearFrom(
                "  PERSON@example.test  ", "synthetic", true)).status());
        assertFalse(firstStore.isLockedOut(bucket));
        assertFalse(secondStore.isLockedOut(bucket));

        secondStore.recordLoginFail(bucket);
        var user = new SecurityUser();
        doReturn(new UsernamePasswordAuthenticationToken(user, null)).when(first.dispatcher()).authenticate(any());
        first.service().login(request(" Person@Example.Test "), ClientType.WEB);
        assertNull(firstRedis.opsForValue().get(SecurityRedisKey.LOGIN_FAIL.format(bucket)));
    }

    private Void failedAttempt(LoginServiceImpl service, String identifier, CountDownLatch ready, CountDownLatch start)
            throws InterruptedException {
        ready.countDown();
        assertTrue(start.await(5, TimeUnit.SECONDS));
        assertThrows(LoginException.class, () -> service.login(request(identifier), ClientType.WEB));
        return null;
    }

    private LoginFixture login(SecurityLoginFailureStore store) {
        var dispatcher = mock(LoginDispatcher.class);
        when(dispatcher.authenticate(any())).thenThrow(new LoginException("synthetic invalid credentials"));
        var port = mock(SecurityAuthenticationPort.class);
        when(port.isLockedOut(anyString())).thenAnswer(call -> store.isLockedOut(call.getArgument(0)));
        doAnswer(call -> {
            store.recordLoginFail(call.getArgument(0));
            return null;
        }).when(port).recordLoginFail(anyString());
        doAnswer(call -> {
            store.clearLoginFail(call.getArgument(0));
            return null;
        }).when(port).clearLoginFail(anyString());
        var service = new LoginServiceImpl(dispatcher,
                new StaticListableBeanFactory().getBeanProvider(AuditService.class), port,
                mock(SecurityContextAccessor.class), mock(AuditRecordFactory.class));
        return new LoginFixture(service, dispatcher);
    }

    private static LoginFrom request(String identifier) {
        var request = new LoginFrom();
        request.setType(LoginType.PASSWORD);
        request.setUsername(identifier);
        return request;
    }

    private SecurityLoginFailureStore tracker(RedisTemplate<String, Object> redis) {
        return new SecurityLoginFailureStore(new SecuritySessionStore(redis, properties,
                new StaticListableBeanFactory().getBeanProvider(SecuritySessionPolicyProvider.class)));
    }

    private RedisTemplate<String, Object> template(LettuceConnectionFactory factory) {
        var redis = new SecRedisConfiguration().redisTemplate(factory,
                new SecJacksonConfiguration().redisObjectMapper(), properties);
        var serializer = new StringRedisSerializer();
        redis.setKeySerializer(new RedisSerializer<String>() {
            @Override
            public byte[] serialize(String value) throws SerializationException {
                return serializer.serialize(keyPrefix + value);
            }

            @Override
            public String deserialize(byte[] bytes) throws SerializationException {
                String value = serializer.deserialize(bytes);
                if (value == null || !value.startsWith(keyPrefix)) {
                    throw new SerializationException("Redis 测试键不属于当前用例");
                }
                return value.substring(keyPrefix.length());
            }
        });
        return redis;
    }

    private void clearOwnKeys() {
        var ownedKeys = new ArrayList<byte[]>();
        try (var connection = firstFactory.getConnection();
                var keys = connection.scan(ScanOptions.scanOptions().match(keyPrefix + "*").count(100).build())) {
            while (keys.hasNext()) {
                byte[] key = keys.next();
                if (!new String(key, StandardCharsets.UTF_8).startsWith(keyPrefix)) {
                    throw new IllegalStateException("Redis 扫描结果不属于当前测试用例");
                }
                ownedKeys.add(key);
            }
            for (byte[] key : ownedKeys) {
                connection.keyCommands().del(key);
            }
        }
    }

    private static LettuceConnectionFactory connection(RedisStandaloneConfiguration configuration) {
        var factory = new LettuceConnectionFactory(configuration);
        factory.afterPropertiesSet();
        factory.start();
        return factory;
    }

    private static RedisStandaloneConfiguration testRedisConfiguration() {
        if (!"5".equals(System.getenv("REDIS_DB"))) {
            throw new IllegalStateException("真实依赖测试只允许 REDIS_DB=5");
        }
        String host = System.getenv("REDIS_HOST");
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("真实依赖测试缺少 REDIS_HOST");
        }
        int port;
        try {
            port = Integer.parseInt(System.getenv("REDIS_PORT"));
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("真实依赖测试缺少有效 REDIS_PORT", exception);
        }
        if (port < 1 || port > 65535) {
            throw new IllegalStateException("真实依赖测试 REDIS_PORT 超出范围");
        }
        var configuration = new RedisStandaloneConfiguration(host, port);
        configuration.setDatabase(5);
        String password = System.getenv("REDIS_PASSWORD");
        if (password != null && !password.isBlank()) {
            configuration.setPassword(RedisPassword.of(password));
        }
        return configuration;
    }

    private record LoginFixture(LoginServiceImpl service, LoginDispatcher dispatcher) {
    }
}

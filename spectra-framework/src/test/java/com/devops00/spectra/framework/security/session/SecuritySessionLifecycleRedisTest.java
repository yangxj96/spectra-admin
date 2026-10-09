/*
 * Copyright 2018-2026 yangxj96
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.devops00.spectra.framework.security.session;

import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.port.security.SecurityToken;
import com.devops00.spectra.common.security.policy.SecuritySessionPolicyProvider;
import com.devops00.spectra.common.security.policy.SessionPolicy;
import com.devops00.spectra.common.security.policy.SessionConcurrencyMode;
import com.devops00.spectra.framework.security.configuration.redis.SecJacksonConfiguration;
import com.devops00.spectra.framework.security.configuration.redis.SecRedisConfiguration;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.token.TokenDigestService;
import com.devops00.spectra.framework.security.session.concurrency.AllowSessionConcurrencyStrategy;
import com.devops00.spectra.framework.security.session.concurrency.KickOldSessionConcurrencyStrategy;
import com.devops00.spectra.framework.security.session.concurrency.RejectNewSessionConcurrencyStrategy;
import com.devops00.spectra.framework.security.session.concurrency.SessionConcurrencyStrategyResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 隔离真实 Redis 验证会话生命周期；仅允许专用测试端口及数据库。
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/07
 */
@EnabledIfSystemProperty(named = "spectra.test.redis.port", matches = "26379")
class SecuritySessionLifecycleRedisTest {
    private LettuceConnectionFactory factory;
    private RedisTemplate<String, Object> redis;
    private SecuritySessionIssueService issuer;
    private SecuritySessionRefreshService refresher;
    private SecuritySessionRevocationService revoker;
    private SecuritySessionReaderService reader;
    private SecurityPrincipal user;
    private SessionPolicy policy;
    private String operatorToken;

    @BeforeEach
    void setUp() {
        var configuration = new RedisStandaloneConfiguration("127.0.0.1", 26379);
        configuration.setDatabase(14);
        factory = new LettuceConnectionFactory(configuration);
        factory.afterPropertiesSet();
        factory.start();
        redis = new SecRedisConfiguration().redisTemplate(factory,
                new SecJacksonConfiguration().redisObjectMapper(), new SecurityProperties());
        policy = SessionPolicy.defaults(30, 300);
        var beans = new StaticListableBeanFactory();
        beans.addBean("policy", (SecuritySessionPolicyProvider) code -> policy);
        var store = new SecuritySessionStore(redis, new SecurityProperties(),
                beans.getBeanProvider(SecuritySessionPolicyProvider.class));
        user = mock(SecurityPrincipal.class);
        when(user.getId()).thenReturn(UUID.randomUUID());
        when(user.getUsername()).thenReturn("isolated-session-test");
        when(user.getAuthorityNames()).thenReturn(List.of());
        revoker = new SecuritySessionRevocationService(store, () -> operatorToken);
        issuer = new SecuritySessionIssueService(store, revoker, new SessionConcurrencyStrategyResolver(List.of(
                new AllowSessionConcurrencyStrategy(), new RejectNewSessionConcurrencyStrategy(),
                new KickOldSessionConcurrencyStrategy(store))));
        refresher = new SecuritySessionRefreshService(store, issuer, revoker, id -> user);
        reader = new SecuritySessionReaderService(store, id -> user);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        try (var connection = factory.getConnection()) {
            connection.serverCommands().flushDb();
        }
        factory.destroy();
    }

    @Test
    void userRevocationIncludesRefreshWhoseAccessExpiredBeforeAnotherLogin() {
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        redis.delete(SecurityRedisKey.SESSION.format(TokenDigestService.digest(first.getAccessToken())));
        issuer.createToken(user, ClientType.WEB);
        revoker.deleteByUserId(user.getId());
        assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(first.getRefreshToken()));
    }

    @Test
    void clientRevocationIncludesEveryAllowedSession() {
        operatorToken = issuer.createToken(user, ClientType.APP).getAccessToken();
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        SecurityToken second = issuer.createToken(user, ClientType.WEB);
        revoker.deleteByUserIdAndClient(user.getId().toString(), ClientType.WEB);
        assertNull(reader.getCurrentUser(first.getAccessToken()));
        assertNull(reader.getCurrentUser(second.getAccessToken()));
        assertNotNull(reader.getCurrentUser(operatorToken));
    }

    @Test
    void idleExpiryRejectsAccessAndRefresh() throws InterruptedException {
        policy = new SessionPolicy(SessionConcurrencyMode.ALLOW, 5, 30, 300, null, 1L);
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        Thread.sleep(1100);
        assertNull(reader.getCurrentUser(first.getAccessToken()));
        assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(first.getRefreshToken()));
    }

    @Test
    void absoluteExpiryCannotBeExtendedByRefresh() throws InterruptedException {
        policy = new SessionPolicy(SessionConcurrencyMode.ALLOW, 5, 30, 300, 1L, null);
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        Thread.sleep(600);
        SecurityToken next = refresher.refreshByRefreshToken(first.getRefreshToken());
        Thread.sleep(550);
        assertNull(reader.getCurrentUser(next.getAccessToken()));
        assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(next.getRefreshToken()));
    }

    @Test
    void refreshPreservesOriginalLoginTime() throws InterruptedException {
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        Object loginTime = redis.opsForHash().get(SecurityRedisKey.SESSION.format(
                TokenDigestService.digest(first.getAccessToken())), "loginTime");
        Thread.sleep(20);
        SecurityToken next = refresher.refreshByRefreshToken(first.getRefreshToken());
        assertEquals(loginTime, redis.opsForHash().get(SecurityRedisKey.SESSION.format(
                TokenDigestService.digest(next.getAccessToken())), "loginTime"));
    }
}

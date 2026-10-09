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
import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 隔离真实 Redis 验证会话生命周期；仅允许专用测试端口及数据库。
 *
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
    void naturallyExpiredAccessStillHasRevocableRefresh() throws InterruptedException {
        policy = SessionPolicy.defaults(1, 30);
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        String accessDigest = TokenDigestService.digest(first.getAccessToken());
        String sessionKey = SecurityRedisKey.SESSION.format(accessDigest);
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (Boolean.TRUE.equals(redis.hasKey(sessionKey)) && System.nanoTime() < deadline) {
            Thread.sleep(20);
        }
        assertFalse(Boolean.TRUE.equals(redis.hasKey(sessionKey)));
        assertTrue(Boolean.TRUE.equals(redis.opsForSet()
                .isMember(SecurityRedisKey.USER_TOKENS.format(user.getId()), accessDigest)));
        revoker.deleteByUserId(user.getId());
        assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(first.getRefreshToken()));
    }

    @Test
    void kickOldRevokesRefreshAfterItsAccessExpired() {
        policy = new SessionPolicy(SessionConcurrencyMode.KICK_OLD, 5, 30, 300, null, null);
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        redis.delete(SecurityRedisKey.SESSION.format(TokenDigestService.digest(first.getAccessToken())));
        SecurityToken replacement = issuer.createToken(user, ClientType.WEB);
        assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(first.getRefreshToken()));
        assertNotNull(reader.getCurrentUser(replacement.getAccessToken()));
    }

    @Test
    void rejectNewCountsRefreshAfterItsAccessExpired() {
        policy = new SessionPolicy(SessionConcurrencyMode.REJECT_NEW, 1, 30, 300, null, null);
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        redis.delete(SecurityRedisKey.SESSION.format(TokenDigestService.digest(first.getAccessToken())));
        assertThrows(IllegalStateException.class, () -> issuer.createToken(user, ClientType.WEB));
        assertNotNull(refresher.refreshByRefreshToken(first.getRefreshToken()));
    }

    @Test
    void clientRevocationIncludesEveryAllowedSession() {
        operatorToken = issuer.createToken(user, ClientType.APP).getAccessToken();
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        SecurityToken second = issuer.createToken(user, ClientType.WEB);
        redis.delete(SecurityRedisKey.SESSION.format(TokenDigestService.digest(first.getAccessToken())));
        revoker.deleteByUserIdAndClient(user.getId().toString(), ClientType.WEB);
        assertNull(reader.getCurrentUser(first.getAccessToken()));
        assertNull(reader.getCurrentUser(second.getAccessToken()));
        assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(first.getRefreshToken()));
        assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(second.getRefreshToken()));
        assertNotNull(reader.getCurrentUser(operatorToken));
    }

    @Test
    void independentRedisConnectionsShareClientRevocation() {
        var configuration = new RedisStandaloneConfiguration("127.0.0.1", 26379);
        configuration.setDatabase(14);
        var otherFactory = new LettuceConnectionFactory(configuration);
        otherFactory.afterPropertiesSet();
        otherFactory.start();
        try {
            RedisTemplate<String, Object> otherRedis = new SecRedisConfiguration().redisTemplate(otherFactory,
                    new SecJacksonConfiguration().redisObjectMapper(), new SecurityProperties());
            var beans = new StaticListableBeanFactory();
            beans.addBean("policy", (SecuritySessionPolicyProvider) code -> policy);
            var otherStore = new SecuritySessionStore(otherRedis, new SecurityProperties(),
                    beans.getBeanProvider(SecuritySessionPolicyProvider.class));
            var otherRevoker = new SecuritySessionRevocationService(otherStore, () -> operatorToken);
            var otherIssuer = new SecuritySessionIssueService(otherStore, otherRevoker,
                    new SessionConcurrencyStrategyResolver(List.of(new AllowSessionConcurrencyStrategy(),
                            new RejectNewSessionConcurrencyStrategy(), new KickOldSessionConcurrencyStrategy(otherStore))));
            operatorToken = issuer.createToken(user, ClientType.APP).getAccessToken();
            SecurityToken first = otherIssuer.createToken(user, ClientType.WEB);
            SecurityToken second = issuer.createToken(user, ClientType.WEB);
            redis.delete(SecurityRedisKey.SESSION.format(TokenDigestService.digest(first.getAccessToken())));
            otherRevoker.deleteByUserIdAndClient(user.getId().toString(), ClientType.WEB);
            assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(first.getRefreshToken()));
            assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(second.getRefreshToken()));
            assertNotNull(reader.getCurrentUser(operatorToken));
        } finally {
            otherFactory.destroy();
        }
    }

    @Test
    void shorterClientPolicyDoesNotExpireLongerRefreshUserIndex() {
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        policy = SessionPolicy.defaults(5, 30);
        issuer.createToken(user, ClientType.APP);
        Long remaining = redis.getExpire(SecurityRedisKey.USER_TOKENS.format(user.getId()));
        assertNotNull(remaining);
        assertTrue(remaining > 100, "另一个客户端的短 Refresh TTL 不得缩短用户索引");
        revoker.deleteByUserId(user.getId());
        assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(first.getRefreshToken()));
    }

    @Test
    void refreshLogoutAfterAccessExpiryKeepsOtherSessionOnline() {
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        SecurityToken second = issuer.createToken(user, ClientType.WEB);
        redis.delete(SecurityRedisKey.SESSION.format(TokenDigestService.digest(first.getAccessToken())));
        revoker.deleteByRefreshToken(first.getRefreshToken());
        assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(first.getRefreshToken()));
        assertNotNull(reader.getCurrentUser(second.getAccessToken()));
        assertTrue(Boolean.TRUE.equals(redis.opsForSet()
                .isMember(SecurityRedisKey.ONLINE_USERS.getPattern(),
                        user.getId().toString())));
    }

    @Test
    void loginPrunesExpiredAccessOnlyAfterItsRefreshIsGone() {
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        String accessDigest = TokenDigestService.digest(first.getAccessToken());
        redis.delete(SecurityRedisKey.SESSION.format(accessDigest));
        redis.delete(SecurityRedisKey.REFRESH_TOKEN.format(TokenDigestService.digest(first.getRefreshToken())));
        issuer.createToken(user, ClientType.WEB);
        assertFalse(Boolean.TRUE.equals(redis.opsForSet()
                .isMember(
                        SecurityRedisKey.USER_TOKENS.format(user.getId()), accessDigest)));
        assertFalse(Boolean.TRUE.equals(redis.hasKey(SecurityRedisKey.REFRESH_TOKEN.format(accessDigest))));
    }

    @Test
    void orphanedRefreshFromOldAccessIndexCannotReissueSession() {
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        String accessDigest = TokenDigestService.digest(first.getAccessToken());
        redis.delete(SecurityRedisKey.SESSION.format(accessDigest));
        redis.opsForSet().remove(SecurityRedisKey.USER_TOKENS.format(user.getId()), accessDigest);
        assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(first.getRefreshToken()));
        assertFalse(Boolean.TRUE.equals(redis.opsForSet()
                .isMember(
                        SecurityRedisKey.USER_TOKENS.format(user.getId()), accessDigest)));
    }

    @Test
    void inconsistentRefreshMappingRejectsUserRevocation() {
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        String refreshDigest = TokenDigestService.digest(first.getRefreshToken());
        redis.opsForHash().put(SecurityRedisKey.REFRESH_TOKEN.format(refreshDigest), "accessToken", "wrong-digest");
        assertThrows(SecurityRedisUnavailableException.class, () -> revoker.deleteByUserId(user.getId()));
        assertNotNull(reader.getCurrentUser(first.getAccessToken()));
    }

    @Test
    void inconsistentRefreshOwnershipRejectsUserRevocation() {
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        String refreshDigest = TokenDigestService.digest(first.getRefreshToken());
        redis.opsForHash().put(SecurityRedisKey.REFRESH_TOKEN.format(refreshDigest), "clientType", "app");
        assertThrows(SecurityRedisUnavailableException.class, () -> revoker.deleteByUserId(user.getId()));
        assertNotNull(reader.getCurrentUser(first.getAccessToken()));
    }

    @Test
    void managementHandleRevokesFamilyAfterAccessExpiry() {
        operatorToken = issuer.createToken(user, ClientType.APP).getAccessToken();
        SecurityToken target = issuer.createToken(user, ClientType.WEB);
        String accessDigest = TokenDigestService.digest(target.getAccessToken());
        Map<?, ?> summary = (Map<?, ?>) redis.opsForValue().get(SecurityRedisKey.SESSION_SUMMARY.format(accessDigest));
        assertNotNull(summary);
        String handle = (String) summary.get("sessionId");
        redis.delete(SecurityRedisKey.SESSION.format(accessDigest));
        revoker.deleteBySessionId(handle);
        assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(target.getRefreshToken()));
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
        Object loginTime = redis.opsForHash()
                .get(SecurityRedisKey.SESSION.format(
                        TokenDigestService.digest(first.getAccessToken())), "loginTime");
        Thread.sleep(20);
        SecurityToken next = refresher.refreshByRefreshToken(first.getRefreshToken());
        assertEquals(loginTime, redis.opsForHash()
                .get(SecurityRedisKey.SESSION.format(
                        TokenDigestService.digest(next.getAccessToken())), "loginTime"));
    }
}

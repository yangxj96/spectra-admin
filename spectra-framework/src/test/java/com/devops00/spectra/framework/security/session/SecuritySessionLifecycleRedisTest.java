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
import com.devops00.spectra.common.port.security.SecurityUserLoader;
import com.devops00.spectra.common.security.policy.SecuritySessionPolicyProvider;
import com.devops00.spectra.common.security.policy.SessionPolicy;
import com.devops00.spectra.common.security.policy.SessionConcurrencyMode;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.store.RefreshTokenRotationStore;
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
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * 使用 test 环境的 Redis DB 5 验证会话生命周期，并隔离每个用例的键。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/07
 */
@EnabledIfSystemProperty(named = "spectra.test.real-deps", matches = "true")
class SecuritySessionLifecycleRedisTest {
    private LettuceConnectionFactory factory;
    private RedisTemplate<String, Object> redis;
    private SecuritySessionStore store;
    private SecuritySessionIssueService issuer;
    private SecuritySessionRefreshService refresher;
    private SecuritySessionRevocationService revoker;
    private SecuritySessionReaderService reader;
    private SecurityPrincipal user;
    private SessionPolicy policy;
    private String operatorToken;
    private String keyPrefix;

    @BeforeEach
    void setUp() {
        var configuration = RealRedisTestEnvironment.configuration();
        factory = new LettuceConnectionFactory(configuration);
        factory.afterPropertiesSet();
        factory.start();
        keyPrefix = RealRedisTestEnvironment.newKeyPrefix();
        redis = RealRedisTestEnvironment.template(factory, keyPrefix);
        policy = SessionPolicy.defaults(30, 300);
        var beans = new StaticListableBeanFactory();
        beans.addBean("policy", (SecuritySessionPolicyProvider) code -> policy);
        store = new SecuritySessionStore(redis, new SecurityProperties(),
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
    void missingUserIndexIsConfirmedEmptyInIsolatedRedis() {
        UUID missingUserId = UUID.randomUUID();
        String userIndexKey = SecurityRedisKey.USER_TOKENS.format(missingUserId);

        assertEquals(Set.of(), store.members("读取不存在的用户会话索引", userIndexKey));
        revoker.deleteByUserId(missingUserId);
        assertFalse(Boolean.TRUE.equals(redis.hasKey(userIndexKey)));
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
        var configuration = RealRedisTestEnvironment.configuration();
        var otherFactory = new LettuceConnectionFactory(configuration);
        otherFactory.afterPropertiesSet();
        otherFactory.start();
        try {
            RedisTemplate<String, Object> otherRedis = RealRedisTestEnvironment.template(otherFactory, keyPrefix);
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
    void replayOfRotatedRefreshRevokesReplacementFamily() {
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        SecurityToken replacement = refresher.refreshByRefreshToken(first.getRefreshToken());
        assertNotNull(reader.getCurrentUser(replacement.getAccessToken()));

        assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(first.getRefreshToken()));
        assertNull(reader.getCurrentUser(replacement.getAccessToken()));
        assertThrows(BadCredentialsException.class,
                () -> refresher.refreshByRefreshToken(replacement.getRefreshToken()));
    }

    @Test
    void readerRejectsLiveAccessFromFencedFamily() {
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        assertNotNull(reader.getCurrentUser(first.getAccessToken()));
        String digest = TokenDigestService.digest(first.getAccessToken());
        Object familyId = redis.opsForHash().get(SecurityRedisKey.SESSION.format(digest), "familyId");
        assertNotNull(familyId);
        redis.opsForValue().set(SecurityRedisKey.REFRESH_REPLAY_FENCE.format(familyId), "REVOKED");

        assertNull(reader.getCurrentUser(first.getAccessToken()));
        assertNull(reader.getCurrentUser());
        assertNotNull(reader.getCurrentUser(issuer.createToken(user, ClientType.APP).getAccessToken()));
    }

    @Test
    void replayBetweenClaimAndReplacementIssueCannotRestoreFamily() throws Exception {
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        String familyId = (String) redis.opsForHash()
                .get(SecurityRedisKey.SESSION.format(
                        TokenDigestService.digest(first.getAccessToken())), "familyId");
        assertNotNull(familyId);
        var configuration = RealRedisTestEnvironment.configuration();
        var otherFactory = new LettuceConnectionFactory(configuration);
        otherFactory.afterPropertiesSet();
        otherFactory.start();
        var executor = Executors.newFixedThreadPool(2);
        var replayLoaded = new CountDownLatch(1);
        var releaseReplay = new CountDownLatch(1);
        var firstAtIssue = new CountDownLatch(1);
        var releaseIssue = new CountDownLatch(1);
        try {
            RedisTemplate<String, Object> otherRedis = RealRedisTestEnvironment.template(otherFactory, keyPrefix);
            var beans = new StaticListableBeanFactory();
            beans.addBean("policy", (SecuritySessionPolicyProvider) code -> policy);
            var otherStore = new SecuritySessionStore(otherRedis, new SecurityProperties(),
                    beans.getBeanProvider(SecuritySessionPolicyProvider.class));
            var otherRevoker = new SecuritySessionRevocationService(otherStore, () -> null);
            SecurityUserLoader replayLoader = id -> {
                replayLoaded.countDown();
                awaitLatch(releaseReplay);
                return user;
            };
            var replaying = new SecuritySessionRefreshService(otherStore, issuer, otherRevoker, replayLoader);
            var pausedIssuer = spy(issuer);
            doAnswer(invocation -> {
                firstAtIssue.countDown();
                awaitLatch(releaseIssue);
                return invocation.callRealMethod();
            }).when(pausedIssuer).createToken(any(SecurityPrincipal.class), any(ClientType.class), anyString());
            var rotating = new SecuritySessionRefreshService(store, pausedIssuer, revoker, id -> user);

            var replayFuture = executor.submit(() -> replaying.refreshByRefreshToken(first.getRefreshToken()));
            awaitLatch(replayLoaded);
            var rotateFuture = executor.submit(() -> rotating.refreshByRefreshToken(first.getRefreshToken()));
            awaitLatch(firstAtIssue);
            releaseReplay.countDown();
            assertInstanceOf(BadCredentialsException.class,
                    assertThrows(ExecutionException.class, () -> replayFuture.get(5, TimeUnit.SECONDS)).getCause());
            releaseIssue.countDown();
            assertInstanceOf(BadCredentialsException.class,
                    assertThrows(ExecutionException.class, () -> rotateFuture.get(5, TimeUnit.SECONDS)).getCause());

            assertNull(reader.getCurrentUser(first.getAccessToken()));
            assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(first.getRefreshToken()));
            assertTrue(Boolean.TRUE.equals(redis.hasKey(SecurityRedisKey.REFRESH_REPLAY_FENCE.format(familyId))));
        } finally {
            releaseReplay.countDown();
            releaseIssue.countDown();
            executor.shutdownNow();
            otherFactory.destroy();
        }
    }

    @Test
    void replayFenceTtlNeverShortensOnRepeatedRevocation() {
        String familyId = UUID.randomUUID().toString();
        RefreshTokenRotationStore.markFamilyRevoked(redis, familyId, 300);
        String fenceKey = SecurityRedisKey.REFRESH_REPLAY_FENCE.format(familyId);
        Long firstTtl = redis.getExpire(fenceKey, TimeUnit.SECONDS);
        assertNotNull(firstTtl);
        RefreshTokenRotationStore.markFamilyRevoked(redis, familyId, 5);
        Long secondTtl = redis.getExpire(fenceKey, TimeUnit.SECONDS);
        assertNotNull(secondTtl);
        assertTrue(secondTtl >= firstTtl - 1);
    }

    @Test
    void replayFenceOutlivesAccessWhenPolicyShrinks() {
        policy = SessionPolicy.defaults(300, 30);
        SecurityToken first = issuer.createToken(user, ClientType.WEB);
        SecurityToken replacement = refresher.refreshByRefreshToken(first.getRefreshToken());
        String replacementDigest = TokenDigestService.digest(replacement.getAccessToken());
        String familyId = (String) redis.opsForHash().get(SecurityRedisKey.SESSION.format(replacementDigest), "familyId");
        assertNotNull(familyId);

        policy = SessionPolicy.defaults(5, 5);
        assertThrows(BadCredentialsException.class, () -> refresher.refreshByRefreshToken(first.getRefreshToken()));
        Long fenceTtl = redis.getExpire(SecurityRedisKey.REFRESH_REPLAY_FENCE.format(familyId), TimeUnit.SECONDS);
        assertNotNull(fenceTtl);
        assertTrue(fenceTtl > 100, "围栏必须覆盖此前签发的较长 Access 生命周期");
        assertNull(reader.getCurrentUser(replacement.getAccessToken()));
    }

    private static void awaitLatch(CountDownLatch latch) {
        try {
            assertTrue(latch.await(5, TimeUnit.SECONDS), "预期的 Redis 交错点未到达");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("等待 Redis 交错点被中断", exception);
        }
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

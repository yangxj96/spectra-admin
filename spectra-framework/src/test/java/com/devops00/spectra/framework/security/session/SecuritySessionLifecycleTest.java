/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.framework.security.session;

import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.common.security.policy.SecuritySessionPolicyProvider;
import com.devops00.spectra.common.security.policy.SessionPolicy;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.session.concurrency.AllowSessionConcurrencyStrategy;
import com.devops00.spectra.framework.security.session.concurrency.KickOldSessionConcurrencyStrategy;
import com.devops00.spectra.framework.security.session.concurrency.RejectNewSessionConcurrencyStrategy;
import com.devops00.spectra.framework.security.session.concurrency.SessionConcurrencyStrategyResolver;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.token.TokenDigestService;
import com.devops00.spectra.framework.security.session.token.SecurityTokenAccessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Redis Security Session 撤销测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class SecuritySessionLifecycleTest {

    @Test
    void shouldNotPersistAuthenticationAssuranceWhenCreatingSession() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        String userTokensKey = SecurityRedisKey.USER_TOKENS.format(userId);

        RedisTemplate<String, Object> redis = mock();
        HashOperations<String, Object, Object> hashes = mock();
        SetOperations<String, Object> sets = mock();
        ValueOperations<String, Object> values = mock();
        when(redis.opsForHash()).thenReturn(hashes);
        when(redis.opsForSet()).thenReturn(sets);
        when(redis.opsForValue()).thenReturn(values);
        when(values.setIfAbsent(anyString(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(java.time.Duration.class))).thenReturn(true);
        when(redis.expire(anyString(), org.mockito.ArgumentMatchers.any(java.time.Duration.class))).thenReturn(true);
        when(sets.members(eq(userTokensKey))).thenReturn(Set.of());
        when(redis.hasKey(anyString())).thenReturn(false);

        SecurityPrincipal user = mock(SecurityPrincipal.class);
        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("devops00.com");
        when(user.getAuthorityNames()).thenReturn(List.of("ROLE_DEV_OPS"));
        SecuritySessionPolicyProvider policyProvider = mock();
        when(policyProvider.find("web")).thenReturn(SessionPolicy.defaults(900, 86400));

        var store = new SecuritySessionStore(redis, new SecurityProperties(), provider(policyProvider));
        var repository = new SecuritySessionIssueService(store,
                new SecuritySessionRevocationService(store, () -> null), resolver(store));

        repository.createToken(user, com.devops00.spectra.common.constant.ClientType.WEB);

        ArgumentCaptor<Map> sessionCaptor = ArgumentCaptor.forClass(Map.class);
        verify(hashes, org.mockito.Mockito.atLeast(2)).putAll(anyString(), sessionCaptor.capture());
        for (Map<?, ?> hash : sessionCaptor.getAllValues()) {
            org.junit.jupiter.api.Assertions.assertFalse(hash.containsKey("aal"));
        }
    }

    @Test
    void shouldDeleteRotatedRefreshTokensAndClaimsWhenLoggingOutByRefreshToken() {
        String refreshToken = "refresh-token";
        String refreshDigest = TokenDigestService.digest(refreshToken);
        String rotatedRefreshDigest = "rotated-refresh-digest";
        String accessDigest = "access-digest";
        String familyId = "family-id";
        String refreshKey = SecurityRedisKey.REFRESH_TOKEN.format(refreshDigest);
        String sessionKey = SecurityRedisKey.SESSION.format(accessDigest);

        RedisTemplate<String, Object> redis = mock();
        HashOperations<String, Object, Object> hashes = mock();
        SetOperations<String, Object> sets = mock();
        when(redis.opsForHash()).thenReturn(hashes);
        when(redis.opsForSet()).thenReturn(sets);
        when(hashes.entries(eq(refreshKey))).thenReturn(Map.of(
                "accessToken", accessDigest,
                "userId", "user-id",
                "familyId", familyId));
        when(hashes.entries(eq(sessionKey))).thenReturn(Map.of());
        when(sets.members(SecurityRedisKey.REFRESH_FAMILY.format(familyId)))
                .thenReturn(Set.of(refreshDigest, rotatedRefreshDigest));

        var repository = new SecuritySessionRevocationService(store(redis, null), () -> null);

        repository.deleteByRefreshToken(refreshToken);

        verify(redis, atLeastOnce()).delete(SecurityRedisKey.REFRESH_TOKEN.format(refreshDigest));
        verify(redis, atLeastOnce()).delete(SecurityRedisKey.REFRESH_CLAIM.format(refreshDigest));
        verify(redis, atLeastOnce()).delete(SecurityRedisKey.REFRESH_TOKEN.format(rotatedRefreshDigest));
        verify(redis, atLeastOnce()).delete(SecurityRedisKey.REFRESH_CLAIM.format(rotatedRefreshDigest));
        verify(redis, atLeastOnce()).delete(SecurityRedisKey.REFRESH_FAMILY.format(familyId));
    }

    @Test
    void shouldDeleteOrphanedRefreshClaimWhenRefreshHashIsAlreadyMissing() {
        String refreshToken = "expired-refresh-token";
        String refreshDigest = TokenDigestService.digest(refreshToken);
        String refreshKey = SecurityRedisKey.REFRESH_TOKEN.format(refreshDigest);

        RedisTemplate<String, Object> redis = mock();
        HashOperations<String, Object, Object> hashes = mock();
        when(redis.opsForHash()).thenReturn(hashes);
        when(hashes.entries(anyString())).thenReturn(Map.of());

        var repository = new SecuritySessionRevocationService(store(redis, null), () -> null);

        repository.deleteByRefreshToken(refreshToken);

        verify(hashes).entries(refreshKey);
        verify(redis).delete(SecurityRedisKey.REFRESH_CLAIM.format(refreshDigest));
    }

    @Test
    void shouldPreserveCurrentAccessSessionWhenRevokingOtherUserSessions() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        String currentToken = "current-access-token";
        String currentDigest = TokenDigestService.digest(currentToken);
        String otherDigest = "other-access-digest";
        String familyId = "family-id";
        String userTokensKey = SecurityRedisKey.USER_TOKENS.format(userId);
        String otherSessionKey = SecurityRedisKey.SESSION.format(otherDigest);

        RedisTemplate<String, Object> redis = mock();
        HashOperations<String, Object, Object> hashes = mock();
        SetOperations<String, Object> sets = mock();
        ValueOperations<String, Object> values = mock();
        when(redis.opsForHash()).thenReturn(hashes);
        when(redis.opsForSet()).thenReturn(sets);
        when(redis.opsForValue()).thenReturn(values);
        when(sets.members(eq(userTokensKey))).thenReturn(Set.of(currentDigest, otherDigest));
        when(sets.members(eq(SecurityRedisKey.REFRESH_FAMILY.format(familyId)))).thenReturn(Set.of());
        when(sets.size(eq(userTokensKey))).thenReturn(1L);
        when(hashes.entries(eq(otherSessionKey))).thenReturn(Map.of(
                "userId", userId.toString(),
                "clientType", "web",
                "familyId", familyId));
        when(values.get(anyString())).thenReturn(null);

        var repository = new SecuritySessionRevocationService(store(redis, null), () -> null);

        repository.deleteByUserIdExceptToken(userId, currentToken);

        verify(redis).delete(otherSessionKey);
        verify(redis, never()).delete(SecurityRedisKey.SESSION.format(currentDigest));
        verify(sets).remove(userTokensKey, otherDigest);
        verify(sets, never()).remove(userTokensKey, currentDigest);
    }

    @Test
    void shouldRemoveExpiredSessionDigestFromUserTokenIndex() {
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        String expiredDigest = "expired-access-digest";
        String refreshDigest = "refresh-digest";
        String familyId = "family-id";
        String userTokensKey = SecurityRedisKey.USER_TOKENS.format(userId);
        String sessionKey = SecurityRedisKey.SESSION.format(expiredDigest);
        String refreshKey = SecurityRedisKey.REFRESH_TOKEN.format(refreshDigest);

        RedisTemplate<String, Object> redis = mock();
        HashOperations<String, Object, Object> hashes = mock();
        SetOperations<String, Object> sets = mock();
        ValueOperations<String, Object> values = mock();
        when(redis.opsForHash()).thenReturn(hashes);
        when(redis.opsForSet()).thenReturn(sets);
        when(redis.opsForValue()).thenReturn(values);
        when(sets.members(eq(userTokensKey))).thenReturn(Set.of(expiredDigest));
        when(sets.members(eq(SecurityRedisKey.REFRESH_FAMILY.format(familyId))))
                .thenReturn(Set.of(refreshDigest));
        when(hashes.entries(eq(sessionKey))).thenReturn(Map.of());
        when(hashes.entries(eq(refreshKey))).thenReturn(Map.of("familyId", familyId));
        when(values.get(eq(SecurityRedisKey.REFRESH_TOKEN.format(expiredDigest)))).thenReturn(refreshDigest);
        when(sets.size(eq(userTokensKey))).thenReturn(0L);

        var repository = new SecuritySessionRevocationService(store(redis, null), () -> null);

        repository.deleteByUserId(userId);

        verify(sets).remove(userTokensKey, expiredDigest);
        verify(redis).delete(userTokensKey);
        verify(sets).remove(SecurityRedisKey.ONLINE_USERS.getPattern(), userId.toString());
        verify(redis).delete(refreshKey);
        verify(redis).delete(SecurityRedisKey.REFRESH_CLAIM.format(refreshDigest));
        verify(redis).delete(SecurityRedisKey.REFRESH_FAMILY.format(familyId));
        verify(redis).delete(SecurityRedisKey.REFRESH_TOKEN.format(expiredDigest));
    }

    @Test
    void shouldRevokeOnlyTheFamilyResolvedFromTheManagementHandle() {
        String handle = "opaque-session-handle";
        String familyId = "target-family";
        String otherAccessDigest = "other-access-digest";
        String accessDigest = "target-access-digest";
        String refreshDigest = "target-refresh-digest";
        String userId = "00000000-0000-0000-0000-000000000001";
        String sessionKey = SecurityRedisKey.SESSION.format(accessDigest);
        String handleKey = SecurityRedisKey.SESSION_HANDLE.format(TokenDigestService.digest(handle));
        String familyHandleKey = SecurityRedisKey.FAMILY_HANDLE.format(familyId);
        String currentToken = "different-client-token";
        String currentFamilyId = "different-client-family";
        var redisValues = new ConcurrentHashMap<String, Object>();
        redisValues.put(handleKey, familyId);
        redisValues.put(familyHandleKey, handle);

        RedisTemplate<String, Object> redis = mock();
        HashOperations<String, Object, Object> hashes = mock();
        SetOperations<String, Object> sets = mock();
        ValueOperations<String, Object> values = mock();
        when(redis.opsForHash()).thenReturn(hashes);
        when(redis.opsForSet()).thenReturn(sets);
        when(redis.opsForValue()).thenReturn(values);
        when(redis.delete(anyString())).thenAnswer(invocation -> redisValues.remove(invocation.getArgument(0)) != null);
        when(values.get(anyString())).thenAnswer(invocation -> redisValues.get(invocation.getArgument(0)));
        when(hashes.entries(eq(sessionKey))).thenReturn(Map.of(
                "userId", userId,
                "clientType", "web",
                "familyId", familyId));
        when(hashes.entries(eq(SecurityRedisKey.SESSION.format(TokenDigestService.digest(currentToken)))))
                .thenReturn(Map.of("familyId", currentFamilyId, "userId", userId, "clientType", "app"));
        when(sets.members(eq(SecurityRedisKey.SESSION_FAMILY.format(familyId))))
                .thenReturn(Set.of(accessDigest));
        when(sets.members(eq(SecurityRedisKey.REFRESH_FAMILY.format(familyId))))
                .thenReturn(Set.of(refreshDigest));
        when(sets.size(eq(SecurityRedisKey.USER_TOKENS.format(userId)))).thenReturn(0L);

        SecurityTokenAccessor tokenAccessor = () -> currentToken;
        var repository = new SecuritySessionRevocationService(store(redis, null), tokenAccessor);

        repository.deleteBySessionId(handle);

        verify(redis).delete(sessionKey);
        verify(redis).delete(SecurityRedisKey.SESSION_SUMMARY.format(accessDigest));
        verify(redis).delete(SecurityRedisKey.REFRESH_TOKEN.format(refreshDigest));
        verify(redis).delete(SecurityRedisKey.REFRESH_CLAIM.format(refreshDigest));
        verify(redis).delete(handleKey);
        verify(redis).delete(familyHandleKey);
        verify(redis, never()).delete(SecurityRedisKey.SESSION.format(otherAccessDigest));
        verify(sets, never()).remove(SecurityRedisKey.ONLINE_SESSIONS.getPattern(), otherAccessDigest);
    }

    @Test
    void shouldRejectRevokingTheCurrentSessionByManagementHandle() {
        String currentToken = "current-access-token";
        String currentDigest = TokenDigestService.digest(currentToken);
        String familyId = "current-family";
        String handle = "current-session-handle";
        String userId = "00000000-0000-0000-0000-000000000001";
        String sessionKey = SecurityRedisKey.SESSION.format(currentDigest);
        String userTokensKey = SecurityRedisKey.USER_TOKENS.format(userId);
        String handleKey = SecurityRedisKey.SESSION_HANDLE.format(TokenDigestService.digest(handle));
        String familyHandleKey = SecurityRedisKey.FAMILY_HANDLE.format(familyId);
        var redisValues = new ConcurrentHashMap<String, Object>();
        redisValues.put(handleKey, familyId);
        redisValues.put(familyHandleKey, handle);

        RedisTemplate<String, Object> redis = mock();
        HashOperations<String, Object, Object> hashes = mock();
        SetOperations<String, Object> sets = mock();
        ValueOperations<String, Object> values = mock();
        when(redis.opsForHash()).thenReturn(hashes);
        when(redis.opsForSet()).thenReturn(sets);
        when(redis.opsForValue()).thenReturn(values);
        when(redis.delete(anyString())).thenAnswer(invocation -> redisValues.remove(invocation.getArgument(0)) != null);
        when(values.get(anyString())).thenAnswer(invocation -> redisValues.get(invocation.getArgument(0)));
        when(hashes.entries(eq(sessionKey))).thenReturn(Map.of(
                "userId", userId,
                "clientType", "web",
                "familyId", familyId));
        when(sets.members(eq(SecurityRedisKey.SESSION_FAMILY.format(familyId)))).thenReturn(Set.of(currentDigest));
        when(sets.members(eq(SecurityRedisKey.REFRESH_FAMILY.format(familyId)))).thenReturn(Set.of());
        when(sets.size(eq(userTokensKey))).thenReturn(0L);

        SecurityTokenAccessor tokenAccessor = () -> currentToken;
        var repository = new SecuritySessionRevocationService(store(redis, null), tokenAccessor);

        assertThrows(AccessDeniedException.class, () -> repository.deleteBySessionId(handle));

        verify(redis, never()).delete(sessionKey);
        verify(sets, never()).remove(SecurityRedisKey.ONLINE_SESSIONS.getPattern(), currentDigest);
    }

    @Test
    void shouldRejectRevokingTheCurrentClientSession() {
        String currentToken = "current-access-token";
        String currentDigest = TokenDigestService.digest(currentToken);
        String userId = "00000000-0000-0000-0000-000000000001";
        RedisTemplate<String, Object> redis = mock();
        HashOperations<String, Object, Object> hashes = mock();
        ValueOperations<String, Object> values = mock();
        when(redis.opsForHash()).thenReturn(hashes);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(anyString())).thenReturn(null);
        when(hashes.entries(eq(SecurityRedisKey.SESSION.format(currentDigest)))).thenReturn(Map.of(
                "userId", userId,
                "clientType", "web",
                "familyId", "current-family"));

        SecurityTokenAccessor tokenAccessor = () -> currentToken;
        var repository = new SecuritySessionRevocationService(store(redis, null), tokenAccessor);

        assertThrows(AccessDeniedException.class,
                () -> repository.deleteByUserIdAndClient(userId, ClientType.WEB));

        verify(redis, never()).delete(anyString());
    }

    /**
     * 处理安全会话相关数据。
     */
    private static SecuritySessionStore store(RedisTemplate<String, Object> redis,
                                              SecuritySessionPolicyProvider policyProvider) {
        return new SecuritySessionStore(redis, new SecurityProperties(), provider(policyProvider));
    }

    /**
     * 处理提供器相关数据。
     */
    private static ObjectProvider<SecuritySessionPolicyProvider> provider(SecuritySessionPolicyProvider policyProvider) {
        ObjectProvider<SecuritySessionPolicyProvider> provider = mock();
        when(provider.getIfAvailable()).thenReturn(policyProvider);
        return provider;
    }

    /**
     * 处理安全会话相关数据。
     */
    private static SessionConcurrencyStrategyResolver resolver(SecuritySessionStore store) {
        return new SessionConcurrencyStrategyResolver(List.of(
                new AllowSessionConcurrencyStrategy(),
                new KickOldSessionConcurrencyStrategy(store),
                new RejectNewSessionConcurrencyStrategy()));
    }
}

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

import com.devops00.spectra.common.security.policy.SecuritySessionPolicyProvider;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import com.devops00.spectra.framework.security.redis.token.TokenDigestService;
import com.devops00.spectra.framework.security.session.token.SecurityTokenAccessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

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

/**
 * 验证撤销会话时完整清理关联 Redis 状态，并且不误删其他会话。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class SecuritySessionRevocationSideEffectsTest {

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
    void shouldRemoveExpiredSessionDigestAndItsRefreshFamilyFromIndexes() {
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

    /**
     * 创建安全会话存储。
     */
    private static SecuritySessionStore store(
                                              RedisTemplate<String, Object> redis,
                                              SecuritySessionPolicyProvider policyProvider) {
        return new SecuritySessionStore(redis, new SecurityProperties(), provider(policyProvider));
    }

    /**
     * 创建策略提供器。
     */
    private static ObjectProvider<SecuritySessionPolicyProvider> provider(
                                                                          SecuritySessionPolicyProvider policyProvider) {
        ObjectProvider<SecuritySessionPolicyProvider> provider = mock();
        when(provider.getIfAvailable()).thenReturn(policyProvider);
        return provider;
    }
}

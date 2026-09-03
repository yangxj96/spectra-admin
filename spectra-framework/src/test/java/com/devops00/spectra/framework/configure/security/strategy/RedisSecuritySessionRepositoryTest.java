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

package com.devops00.spectra.framework.configure.security.strategy;

import com.devops00.spectra.framework.configure.security.redis.SecurityRedisKey;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.port.security.SecurityUserLoader;
import com.devops00.spectra.common.security.policy.SecuritySessionPolicyProvider;
import com.devops00.spectra.framework.configure.security.properties.SecurityProperties;
import com.devops00.spectra.common.security.policy.SessionPolicy;
import com.devops00.spectra.framework.configure.security.redis.TokenDigestService;
import com.devops00.spectra.framework.configure.security.converter.UserOnlineConverter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Redis Security Session 撤销测试。
 */
class RedisSecuritySessionRepositoryTest {

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
        when(sets.members(eq(userTokensKey))).thenReturn(Set.of());
        when(redis.hasKey(anyString())).thenReturn(false);

        SecurityPrincipal user = mock(SecurityPrincipal.class);
        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("devops00.com");
        when(user.getAuthorityNames()).thenReturn(List.of("ROLE_DEV_OPS"));
        SecuritySessionPolicyProvider policyProvider = mock();
        when(policyProvider.find("web")).thenReturn(SessionPolicy.defaults(900, 86400));

        var repository = new RedisSecuritySessionRepository(mock(ObjectMapper.class), redis,
                new SecurityProperties(), mock(UserOnlineConverter.class), policyProvider, mock(SecurityUserLoader.class));

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

        var repository = new RedisSecuritySessionRepository(mock(ObjectMapper.class), redis,
                new SecurityProperties(), mock(UserOnlineConverter.class), null, null);

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

        var repository = new RedisSecuritySessionRepository(mock(ObjectMapper.class), redis,
                new SecurityProperties(), mock(UserOnlineConverter.class), null, null);

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

        var repository = new RedisSecuritySessionRepository(mock(ObjectMapper.class), redis,
                new SecurityProperties(), mock(UserOnlineConverter.class), null, null);

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

        var repository = new RedisSecuritySessionRepository(mock(ObjectMapper.class), redis,
                new SecurityProperties(), mock(UserOnlineConverter.class), null, null);

        repository.deleteByUserId(userId);

        verify(sets).remove(userTokensKey, expiredDigest);
        verify(redis).delete(userTokensKey);
        verify(sets).remove(SecurityRedisKey.ONLINE_USERS.getPattern(), userId.toString());
        verify(redis).delete(refreshKey);
        verify(redis).delete(SecurityRedisKey.REFRESH_CLAIM.format(refreshDigest));
        verify(redis).delete(SecurityRedisKey.REFRESH_FAMILY.format(familyId));
        verify(redis).delete(SecurityRedisKey.REFRESH_TOKEN.format(expiredDigest));
    }
}

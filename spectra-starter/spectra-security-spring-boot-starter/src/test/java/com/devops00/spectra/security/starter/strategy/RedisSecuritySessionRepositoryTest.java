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

package com.devops00.spectra.security.starter.strategy;

import com.devops00.spectra.security.base.constant.SecurityRedisKey;
import com.devops00.spectra.security.base.holder.SecurityUserLoader;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.base.util.TokenDigestService;
import com.devops00.spectra.security.starter.web.javabean.converter.UserOnlineConverter;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Redis Security Session 撤销测试。
 */
class RedisSecuritySessionRepositoryTest {

    @Test
    void shouldPreserveMfaAssuranceWhenRefreshingDevOpsSession() {
        String refreshToken = "refresh-token";
        String refreshDigest = TokenDigestService.digest(refreshToken);
        String accessDigest = "access-digest";
        String familyId = "family-id";
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        String refreshKey = SecurityRedisKey.REFRESH_TOKEN.format(refreshDigest);
        String sessionKey = SecurityRedisKey.SESSION.format(accessDigest);

        RedisTemplate<String, Object> redis = mock();
        HashOperations<String, Object, Object> hashes = mock();
        SetOperations<String, Object> sets = mock();
        ValueOperations<String, Object> values = mock();
        SecurityUserLoader userLoader = mock();
        when(redis.opsForHash()).thenReturn(hashes);
        when(redis.opsForSet()).thenReturn(sets);
        when(redis.opsForValue()).thenReturn(values);
        when(hashes.entries(eq(refreshKey))).thenReturn(Map.of(
                "accessToken", accessDigest,
                "userId", userId.toString(),
                "clientType", "WEB",
                "familyId", familyId,
                "aal", "AAL2"));
        // Access Session 已过期时仍必须依据 Refresh Hash 保留的 AAL2 刷新成功。
        when(hashes.entries(eq(sessionKey))).thenReturn(Map.of());
        when(sets.members(anyString())).thenReturn(Set.of());
        when(redis.hasKey(anyString())).thenReturn(false);
        when(redis.execute(any(), anyList(), any())).thenReturn(1L);

        SecurityUser user = new SecurityUser();
        user.setId(userId);
        user.setEmail("devops00.com");
        user.setAuthorities(List.of(new SimpleGrantedAuthority("ROLE_DEV_OPS")));
        when(userLoader.load(userId)).thenReturn(user);

        var repository = new RedisSecuritySessionRepository(mock(ObjectMapper.class), redis,
                new SecurityProperties(), mock(UserOnlineConverter.class), null, userLoader);

        assertDoesNotThrow(() -> repository.refreshByRefreshToken(refreshToken));
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
}

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
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.base.util.TokenDigestService;
import com.devops00.spectra.security.starter.web.javabean.converter.UserOnlineConverter;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Set;

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

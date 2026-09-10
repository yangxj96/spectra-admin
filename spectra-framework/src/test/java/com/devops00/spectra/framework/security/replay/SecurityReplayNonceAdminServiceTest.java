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

package com.devops00.spectra.framework.security.replay;

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.common.security.crypto.digest.SHA256Utils;
import com.devops00.spectra.common.port.security.SecurityReplayNonceAdminPort;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Web 加密 nonce 管理的安全边界测试。 */
class SecurityReplayNonceAdminServiceTest {

    @Test
    void shouldInvalidateOnlyTheHashedCryptoNonce() {
        RedisTemplate<String, Object> redis = redisTemplate();
        ValueOperations<String, Object> values = redis.opsForValue();
        when(values.setIfAbsent(anyString(), any(), any(Duration.class))).thenReturn(true);
        var service = new SecurityReplayNonceAdminService(redis, properties());

        SecurityReplayNonceAdminPort.Result result = service.invalidate("nonce-for-test");

        String expectedKey = SecurityRedisKey.CRYPTO_NONCE.format(SHA256Utils.hash("nonce-for-test"));
        verify(values).setIfAbsent(eq(expectedKey), anyLong(), eq(Duration.ofSeconds(300)));
        verify(redis, never()).delete(anyString());
        assertEquals("TARGET", result.operationType());
        assertEquals(1L, result.affectedCount());
    }

    @Test
    void shouldPersistCutoffBeforeScanningAndDeletingOldNonceKeys() {
        RedisTemplate<String, Object> redis = redisTemplate();
        ValueOperations<String, Object> values = redis.opsForValue();
        when(redis.execute(any(org.springframework.data.redis.core.RedisCallback.class))).thenReturn(2L);
        var service = new SecurityReplayNonceAdminService(redis, properties());

        SecurityReplayNonceAdminPort.Result result = service.invalidateAll();

        var order = inOrder(values, redis);
        order.verify(values).set(eq(SecurityRedisKey.CRYPTO_NONCE_CUTOFF.getPattern()), anyLong(),
                eq(Duration.ofSeconds(300)));
        order.verify(redis).execute(any(org.springframework.data.redis.core.RedisCallback.class));
        verify(redis, never()).delete(SecurityRedisKey.REFRESH_CLAIM.format("refresh-digest"));
        verify(redis, never()).delete(SecurityRedisKey.REFRESH_REPLAY_FENCE.format("family"));
        assertEquals("ALL", result.operationType());
        assertEquals(2L, result.affectedCount());
        assertTrue(result.cutoffEpochSecond() > 0);
    }

    @Test
    void shouldKeepCutoffKeyOutsideTheNonceCleanupScope() {
        assertTrue(SecurityRedisKey.CRYPTO_NONCE_CUTOFF.getPattern()
                .startsWith(SecurityRedisKey.CRYPTO_NONCE.getPattern().substring(0,
                        SecurityRedisKey.CRYPTO_NONCE.getPattern().indexOf("%s"))));
        assertTrue(SecurityRedisKey.CRYPTO_NONCE_CUTOFF.getPattern().contains("cutoff"));
    }

    @Test
    void shouldRejectRequestsAtOrBeforeThePersistedCutoff() {
        RedisTemplate<String, Object> redis = redisTemplate();
        when(redis.opsForValue().get(SecurityRedisKey.CRYPTO_NONCE_CUTOFF.getPattern())).thenReturn(100L);
        var service = new SecurityReplayNonceAdminService(redis, properties());

        assertTrue(service.isBeforeOrAtCutoff(100L));
        assertFalse(service.isBeforeOrAtCutoff(101L));
    }

    @Test
    void shouldFailClosedWhenSecurityRedisCannotPersistNonce() {
        RedisTemplate<String, Object> redis = redisTemplate();
        when(redis.opsForValue().setIfAbsent(anyString(), any(), any(Duration.class)))
                .thenThrow(new DataAccessResourceFailureException("redis unavailable"));
        var service = new SecurityReplayNonceAdminService(redis, properties());

        assertThrows(SecurityRedisUnavailableException.class, () -> service.invalidate("nonce-for-test"));
    }

    private static SecurityProperties properties() {
        return new SecurityProperties();
    }

    @SuppressWarnings("unchecked")
    private static RedisTemplate<String, Object> redisTemplate() {
        var redis = (RedisTemplate<String, Object>) mock(RedisTemplate.class);
        when(redis.opsForValue()).thenReturn(mock(ValueOperations.class));
        return redis;
    }
}

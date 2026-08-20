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

package com.devops00.spectra.core.security.initialization.service.impl;

import com.devops00.spectra.security.base.constant.SecurityRedisKey;
import com.devops00.spectra.security.base.exception.SecurityRedisUnavailableException;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 系统初始化令牌管理测试。 */
class SystemInitializationTokenManagerTest {

    private static final String TOKEN_KEY = SecurityRedisKey.INITIALIZATION_TOKEN.getPattern();

    @Test
    void shouldCreateOnlyDigestWhenTokenDoesNotExist() {
        RedisTemplate<String, Object> redis = mock();
        ValueOperations<String, Object> values = mock();
        when(redis.opsForValue()).thenReturn(values);
        when(values.setIfAbsent(eq(TOKEN_KEY), anyString())).thenReturn(true);

        new SystemInitializationTokenManager(redis).ensureToken();

        var digest = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(values).setIfAbsent(eq(TOKEN_KEY), digest.capture());
        assertTrue(digest.getValue().matches("[0-9a-f]{64}"));
    }

    @Test
    void shouldValidateDigestWithConstantTimeComparison() {
        RedisTemplate<String, Object> redis = mock();
        ValueOperations<String, Object> values = mock();
        when(redis.opsForValue()).thenReturn(values);
        String token = "bootstrap-token";
        when(values.get(TOKEN_KEY)).thenReturn(SystemInitializationTokenManager.digest(token));
        var manager = new SystemInitializationTokenManager(redis);

        assertDoesNotThrow(() -> manager.assertToken(token));
        assertThrows(AccessDeniedException.class, () -> manager.assertToken("wrong-token"));
    }

    @Test
    void shouldFailClosedWhenRedisDoesNotReturnTokenDigest() {
        RedisTemplate<String, Object> redis = mock();
        ValueOperations<String, Object> values = mock();
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(TOKEN_KEY)).thenReturn(null);

        assertThrows(SecurityRedisUnavailableException.class,
                () -> new SystemInitializationTokenManager(redis).assertToken("bootstrap-token"));
    }

    @Test
    void shouldDeleteDigestAfterInitialization() {
        RedisTemplate<String, Object> redis = mock();
        when(redis.delete(TOKEN_KEY)).thenReturn(Boolean.TRUE);

        new SystemInitializationTokenManager(redis).clear();

        verify(redis).delete(TOKEN_KEY);
    }
}

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
import com.devops00.spectra.security.base.constant.SecurityRedisNamespace;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.base.util.RefreshTokenRotationStore;
import com.devops00.spectra.security.base.util.VerificationCodeRedisStore;
import com.devops00.spectra.security.starter.converter.UserOnlineConverter;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Redis 安全契约组合测试。
 *
 * <p>本测试不启动 Docker 或连接开发者本机 Redis；真实 Redis 连通性由显式开启的
 * {@link SecurityRedisFailureIntegrationTest} 验证，避免普通 Maven 测试意外依赖外部服务。</p>
 */
class RedisSecurityContractIntegrationTest {

    @Test
    void shouldKeepAllSecurityKeysInsideTheFixedNamespace() {
        for (SecurityRedisKey key : SecurityRedisKey.values()) {
            assertTrue(key.getPattern().startsWith(SecurityRedisNamespace.PREFIX), key.name());
        }
    }

    @Test
    void shouldConsumeRefreshTokenOnlyOnce() {
        RedisTemplate<String, Object> redis = mock();
        List<String> keys = List.of(
                SecurityRedisKey.REFRESH_TOKEN.format("refresh-digest"),
                SecurityRedisKey.REFRESH_CLAIM.format("refresh-digest"));
        when(redis.execute(any(RedisScript.class), eq(keys), eq(604800L)))
                .thenReturn(1L)
                .thenReturn(0L);

        assertEquals(RefreshTokenRotationStore.ClaimResult.CLAIMED,
                RefreshTokenRotationStore.claim(redis, keys.get(0), keys.get(1), 604800L));
        assertEquals(RefreshTokenRotationStore.ClaimResult.REPLAY,
                RefreshTokenRotationStore.claim(redis, keys.get(0), keys.get(1), 604800L));
    }

    @Test
    void shouldRejectConsumedOrExpiredVerificationCode() {
        RedisTemplate<String, Object> redis = mock();
        String key = "security:verification:login:sms:expired";
        when(redis.execute(any(RedisScript.class), eq(List.of(key)), eq("digest")))
                .thenReturn(0L)
                .thenReturn(0L);

        assertFalse(VerificationCodeRedisStore.compareAndDelete(redis, key, "digest"));
        assertFalse(VerificationCodeRedisStore.compareAndDelete(redis, key, "digest"));
    }

    @Test
    void shouldPreserveFailClosedWhenRedisCommandFails() {
        RedisTemplate<String, Object> redis = mock();
        when(redis.execute(any(RedisScript.class), any(List.class), any(Object.class)))
                .thenThrow(new DataAccessResourceFailureException("redis unavailable"));

        assertThrows(com.devops00.spectra.security.base.exception.SecurityRedisUnavailableException.class,
                () -> VerificationCodeRedisStore.compareAndDelete(redis, "sec:test:code", "digest"));
    }

    @Test
    void shouldFailClosedForRefreshRotationWhenRedisCommandFails() {
        RedisTemplate<String, Object> redis = mock();
        when(redis.execute(any(RedisScript.class), any(List.class), any(Object.class)))
                .thenThrow(new DataAccessResourceFailureException("redis unavailable"));

        assertThrows(com.devops00.spectra.security.base.exception.SecurityRedisUnavailableException.class,
                () -> RefreshTokenRotationStore.claim(redis, "sec:rt:missing", "sec:rt:claim:missing", 300));
    }

    @Test
    void shouldApplyTtlToLoginFailureCounter() {
        RedisTemplate<String, Object> redis = mock();
        ValueOperations<String, Object> values = mock();
        when(redis.opsForValue()).thenReturn(values);
        String username = "root@example.com";
        String key = SecurityRedisKey.LOGIN_FAIL.format(username);
        when(values.increment(key)).thenReturn(1L);

        var properties = new SecurityProperties();
        properties.setLockoutSeconds(300L);
        var repository = new RedisSecuritySessionRepository(mock(ObjectMapper.class), redis, properties,
                mock(UserOnlineConverter.class), null, null);

        repository.recordLoginFail(username);

        verify(redis).expire(key, Duration.ofSeconds(300L));
    }

    @Test
    void shouldFailClosedForLoginFailureStateWhenRedisCommandFails() {
        RedisTemplate<String, Object> redis = mock();
        ValueOperations<String, Object> values = mock();
        when(redis.opsForValue()).thenReturn(values);
        when(values.get(SecurityRedisKey.LOGIN_FAIL.format("root@example.com")))
                .thenThrow(new DataAccessResourceFailureException("redis unavailable"));

        var repository = new RedisSecuritySessionRepository(mock(ObjectMapper.class), redis,
                new SecurityProperties(), mock(UserOnlineConverter.class), null, null);

        assertThrows(com.devops00.spectra.security.base.exception.SecurityRedisUnavailableException.class,
                () -> repository.isLockedOut("root@example.com"));
    }

    @Test
    void shouldNotTurnAValidBusinessResultIntoInfrastructureFailure() {
        RedisTemplate<String, Object> redis = mock();
        when(redis.execute(any(RedisScript.class), eq(List.of("sec:test:code")), eq("digest")))
                .thenReturn(0L);

        assertFalse(VerificationCodeRedisStore.compareAndDelete(redis, "sec:test:code", "digest"));
    }
}

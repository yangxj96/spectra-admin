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

import com.devops00.spectra.security.base.constant.ClientType;
import com.devops00.spectra.security.base.constant.SecurityRedisKey;
import com.devops00.spectra.security.base.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Redis MFA 预认证挑战存储测试。 */
class RedisMfaLoginChallengeRepositoryTest {

    @Test
    @SuppressWarnings("unchecked")
    void shouldPassFailureLimitAsNumericScriptArgument() {
        var arguments = new AtomicReference<Object[]>();
        RedisTemplate<String, Object> redis = new RedisTemplate<>() {
            @Override
            public <T> T execute(RedisScript<T> script, List<String> keys, Object... args) {
                arguments.set(args);
                return (T) Long.valueOf(1L);
            }
        };
        SecurityProperties properties = new SecurityProperties();
        properties.setMfaChallengeMaxAttempts(2);

        var repository = new RedisMfaLoginChallengeRepository(redis, properties);

        repository.recordFailure(UUID.randomUUID().toString());
        assertEquals(1, arguments.get().length);
        assertTrue(arguments.get()[0] instanceof Number);
        assertEquals(2, ((Number) arguments.get()[0]).intValue());
    }

    @Test
    void shouldCreateShortLivedChallengeWithNonSensitiveState() {
        RedisTemplate<String, Object> redis = mock();
        HashOperations<String, Object, Object> hashes = mock();
        when(redis.opsForHash()).thenReturn(hashes);
        SecurityProperties properties = new SecurityProperties();
        properties.setMfaChallengeExpire(300L);

        UUID userId = UUID.randomUUID();
        var repository = new RedisMfaLoginChallengeRepository(redis, properties);

        var challenge = repository.create(userId, "root@example.com", ClientType.WEB, true);

        String key = SecurityRedisKey.MFA_CHALLENGE.format(challenge.id());
        assertEquals(userId, challenge.userId());
        assertEquals(ClientType.WEB, challenge.clientType());
        assertTrue(challenge.enrollmentRequired());
        assertFalse(challenge.enrollmentCompleted());
        verify(hashes).putAll(eq(key), anyMap());
        verify(redis).expire(eq(key), eq(Duration.ofSeconds(300)));
    }

    @Test
    void shouldReadVerificationChallengeFromRedis() {
        RedisTemplate<String, Object> redis = mock();
        HashOperations<String, Object, Object> hashes = mock();
        when(redis.opsForHash()).thenReturn(hashes);
        String challengeId = UUID.randomUUID().toString();
        UUID userId = UUID.randomUUID();
        String key = SecurityRedisKey.MFA_CHALLENGE.format(challengeId);
        when(hashes.entries(key)).thenReturn(Map.of(
                "userId", userId.toString(),
                "username", "root@example.com",
                "clientType", "web",
                "state", "VERIFICATION_REQUIRED",
                "expiresAt", "1890000000000"));

        var challenge = new RedisMfaLoginChallengeRepository(redis, new SecurityProperties()).find(challengeId);

        assertEquals(userId, challenge.userId());
        assertEquals("root@example.com", challenge.username());
        assertEquals(ClientType.WEB, challenge.clientType());
        assertFalse(challenge.enrollmentRequired());
        assertFalse(challenge.enrollmentCompleted());
    }

    @Test
    void shouldInvalidateChallengeWhenFailureLimitIsReached() {
        RedisTemplate<String, Object> redis = mock();
        SecurityProperties properties = new SecurityProperties();
        properties.setMfaChallengeMaxAttempts(2);
        String challengeId = UUID.randomUUID().toString();
        String key = SecurityRedisKey.MFA_CHALLENGE.format(challengeId);
        List<String> keys = List.of(key);
        when(redis.execute(any(RedisScript.class), eq(keys), eq(2)))
                .thenReturn(1L)
                .thenReturn(0L);

        var repository = new RedisMfaLoginChallengeRepository(redis, properties);

        assertTrue(repository.recordFailure(challengeId));
        assertFalse(repository.recordFailure(challengeId));
    }

    @Test
    void shouldRejectWhenRedisDoesNotReturnFailureCounterResult() {
        RedisTemplate<String, Object> redis = mock();
        String challengeId = UUID.randomUUID().toString();
        List<String> keys = List.of(SecurityRedisKey.MFA_CHALLENGE.format(challengeId));
        when(redis.execute(any(RedisScript.class), eq(keys), eq(5))).thenReturn(null);

        var repository = new RedisMfaLoginChallengeRepository(redis, new SecurityProperties());

        assertThrows(SecurityRedisUnavailableException.class, () -> repository.recordFailure(challengeId));
    }
}

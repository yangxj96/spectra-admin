/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.framework.security.redis.store.RefreshTokenRotationStore;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证 Refresh 重放围栏对 Redis 未知结果和故障保持拒绝。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/09
 */
class RefreshTokenRotationStoreTest {

    private static final RefreshTokenRotationStore.ClaimIdentity IDENTITY = new RefreshTokenRotationStore.ClaimIdentity("refresh-digest",
            "access-digest", "user-id", "family-id");

    @Test
    void nullClaimResultFailsClosed() {
        RedisTemplate<String, Object> redis = mock();
        when(redis.execute(any(RedisScript.class), anyList(), any(), any())).thenReturn(null);

        assertThrows(SecurityRedisUnavailableException.class,
                () -> RefreshTokenRotationStore.claim(redis, IDENTITY, 300));
    }

    @Test
    void unexpectedClaimResultFailsClosed() {
        RedisTemplate<String, Object> redis = mock();
        when(redis.execute(any(RedisScript.class), anyList(), any(), any())).thenReturn(99L);

        assertThrows(SecurityRedisUnavailableException.class,
                () -> RefreshTokenRotationStore.claim(redis, IDENTITY, 300));
    }

    @Test
    void failedFenceCommandFailsClosed() {
        RedisTemplate<String, Object> redis = mock();
        when(redis.execute(any(RedisScript.class), anyList(), any())).thenThrow(
                new RedisConnectionFailureException("synthetic isolated Redis failure"));

        assertThrows(SecurityRedisUnavailableException.class,
                () -> RefreshTokenRotationStore.markFamilyRevoked(redis, "family-id", 300));
    }
}

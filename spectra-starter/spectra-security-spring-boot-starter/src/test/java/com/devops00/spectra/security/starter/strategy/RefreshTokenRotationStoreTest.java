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

import com.devops00.spectra.security.base.util.RefreshTokenRotationStore;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Refresh Token Rotation 原子消费结果测试。
 */
class RefreshTokenRotationStoreTest {

    @Test
    void shouldMapFirstClaimToClaimed() {
        var redis = mock(RedisTemplate.class);
        when(redis.execute(any(RedisScript.class), eq(List.of("auth:rt:refresh", "auth:rt:claim")), eq(604800L)))
                .thenReturn(1L);

        assertEquals(RefreshTokenRotationStore.ClaimResult.CLAIMED,
                RefreshTokenRotationStore.claim(redis, "auth:rt:refresh", "auth:rt:claim", 604800L));
    }

    @Test
    void shouldMapSecondClaimToReplay() {
        var redis = mock(RedisTemplate.class);
        when(redis.execute(any(RedisScript.class), eq(List.of("auth:rt:refresh", "auth:rt:claim")), eq(604800L)))
                .thenReturn(0L);

        assertEquals(RefreshTokenRotationStore.ClaimResult.REPLAY,
                RefreshTokenRotationStore.claim(redis, "auth:rt:refresh", "auth:rt:claim", 604800L));
    }

    @Test
    void shouldFailClosedWhenRedisReturnsMissingResult() {
        var redis = mock(RedisTemplate.class);
        when(redis.execute(any(RedisScript.class), eq(List.of("auth:rt:refresh", "auth:rt:claim")), eq(604800L)))
                .thenReturn(null);

        assertEquals(RefreshTokenRotationStore.ClaimResult.MISSING,
                RefreshTokenRotationStore.claim(redis, "auth:rt:refresh", "auth:rt:claim", 604800L));
    }
}

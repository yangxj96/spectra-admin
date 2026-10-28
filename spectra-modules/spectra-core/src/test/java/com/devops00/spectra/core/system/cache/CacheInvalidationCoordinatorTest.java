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

package com.devops00.spectra.core.system.cache;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证缓存清理操作 ID 绑定意图、成功回执复用和失败后重试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/28
 */
class CacheInvalidationCoordinatorTest {

    private final CacheRegionRegistry registry = mock(CacheRegionRegistry.class);
    private final RedisTemplate<String, Object> redis = mock(RedisTemplate.class);
    private final CacheInvalidationCoordinator coordinator = new CacheInvalidationCoordinator(registry, redis);
    private final CacheRegionDescriptor descriptor = new CacheRegionDescriptor("core:dept", "部门", "redis",
            "REMOTE", 60L, false, true);

    @Test
    void sameOperationIdMustKeepSameIntent() {
        stubSuccess();
        var operationId = UUID.randomUUID();

        var first = coordinator.clear(Set.of("core:dept"), false, true, operationId);
        var replay = coordinator.clear(Set.of("core:dept"), false, true, operationId);

        assertEquals("SUCCEEDED", first.status());
        assertEquals(first, replay);
        assertThrows(IllegalArgumentException.class,
                () -> coordinator.clear(Set.of(), true, true, operationId));
    }

    @Test
    void partialOperationCanBeRetried() {
        when(registry.validate(anyCollection())).thenReturn(List.of(descriptor));
        when(registry.clear(anyCollection(), eq(false))).thenReturn(1L);
        when(redis.convertAndSend(anyString(), any())).thenThrow(new RedisConnectionFailureException("redis down"))
                .thenReturn(1L);
        var operationId = UUID.randomUUID();

        assertEquals("PARTIAL", coordinator.clear(Set.of("core:dept"), false, true, operationId).status());
        assertEquals("SUCCEEDED", coordinator.clear(Set.of("core:dept"), false, true, operationId).status());
    }

    private void stubSuccess() {
        when(registry.validate(anyCollection())).thenReturn(List.of(descriptor));
        when(registry.clear(anyCollection(), eq(false))).thenReturn(1L);
        when(redis.convertAndSend(anyString(), any())).thenReturn(1L);
    }
}

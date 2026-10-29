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

package com.devops00.spectra.framework.cache;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link CacheConsistencyVerifier} 恢复一致性校验的单元行为测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
class CacheConsistencyVerifierTest {

    @SuppressWarnings("unchecked")
    private static <T> ObjectProvider<T> provider(T value) {
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(value);
        return provider;
    }

    private static CacheManager cacheManager(Cache cache, Set<String> cacheNames) {
        CacheManager cacheManager = mock(CacheManager.class);
        when(cacheManager.getCache(CacheConsistencyVerifier.PROBE_CACHE_NAME)).thenReturn(cache);
        when(cacheManager.getCacheNames()).thenReturn(cacheNames);
        return cacheManager;
    }

    @Test
    void cacheRoundTripPassesAndProbeIsRemoved() {
        Cache cache = mock(Cache.class);
        CacheManager cacheManager = cacheManager(cache, Set.of("core:dept"));
        AtomicReference<Object> stored = new AtomicReference<>();
        doAnswer(invocation -> {
            stored.set(invocation.getArgument(1));
            return null;
        }).when(cache).put(eq(CacheConsistencyVerifier.PROBE_KEY), any());
        when(cache.get(CacheConsistencyVerifier.PROBE_KEY))
                .thenAnswer(invocation -> (Cache.ValueWrapper) stored::get);

        var verifier = new CacheConsistencyVerifier(provider(cacheManager));

        assertDoesNotThrow(verifier::verifyRecovery);
        assertNotNull(stored.get());
        verify(cache, times(2)).evict(CacheConsistencyVerifier.PROBE_KEY);
    }

    @Test
    void missingCacheManagerRejectsRecovery() {
        var verifier = new CacheConsistencyVerifier(provider(null));

        assertThrows(IllegalStateException.class, verifier::verifyRecovery);
    }

    @Test
    void missingProbeCacheRejectsRecovery() {
        CacheManager cacheManager = mock(CacheManager.class);
        when(cacheManager.getCache(CacheConsistencyVerifier.PROBE_CACHE_NAME)).thenReturn(null);
        var verifier = new CacheConsistencyVerifier(provider(cacheManager));

        assertThrows(IllegalStateException.class, verifier::verifyRecovery);
        verify(cacheManager, never()).getCacheNames();
    }

    @Test
    void securityNamespaceOverlapRejectsRecovery() {
        Cache cache = mock(Cache.class);
        CacheManager cacheManager = cacheManager(cache, Set.of("sec"));
        var verifier = new CacheConsistencyVerifier(provider(cacheManager));

        assertThrows(IllegalStateException.class, verifier::verifyRecovery);
        verify(cache, never()).put(any(), any());
    }

    @Test
    void unexpectedCachedValueRejectsRecovery() {
        Cache cache = mock(Cache.class);
        CacheManager cacheManager = cacheManager(cache, Set.of("core:dept"));
        when(cache.get(CacheConsistencyVerifier.PROBE_KEY))
                .thenReturn((Cache.ValueWrapper) () -> List.of(UUID.randomUUID()));
        var verifier = new CacheConsistencyVerifier(provider(cacheManager));

        assertThrows(IllegalStateException.class, verifier::verifyRecovery);
    }

    @Test
    void cacheCommandFailureRejectsRecoveryAndCleansProbe() {
        Cache cache = mock(Cache.class);
        CacheManager cacheManager = cacheManager(cache, Set.of("core:dept"));
        doThrow(new IllegalStateException("redis down"))
                .when(cache)
                .put(eq(CacheConsistencyVerifier.PROBE_KEY), any());
        var verifier = new CacheConsistencyVerifier(provider(cacheManager));

        assertThrows(IllegalStateException.class, verifier::verifyRecovery);
        verify(cache, times(2)).evict(CacheConsistencyVerifier.PROBE_KEY);
    }
}

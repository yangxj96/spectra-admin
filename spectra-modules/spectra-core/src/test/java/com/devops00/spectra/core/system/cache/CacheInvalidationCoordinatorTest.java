/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.system.cache;

import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CacheInvalidationCoordinatorTest {

    @Test
    void shouldClearRegisteredRegionLocallyAndMakeRepeatedOperationIdempotent() {
        Cache cache = mock(Cache.class);
        var manager = new SimpleCacheManager();
        manager.setCaches(List.of(cache));
        when(cache.getName()).thenReturn("core:dept");
        manager.initializeCaches();
        var registry = new CacheRegionRegistry(manager);
        RedisTemplate<String, Object> redis = mock(RedisTemplate.class);
        var coordinator = new CacheInvalidationCoordinator(registry, redis);
        UUID operationId = UUID.randomUUID();

        var first = coordinator.clear(Set.of("core:dept"), false, false, operationId);
        var second = coordinator.clear(Set.of("core:dept"), false, false, operationId);

        verify(cache).clear();
        assertEquals("SUCCEEDED", first.status());
        assertEquals("SUCCEEDED", second.status());
        assertEquals(operationId, first.operationId());
    }

    @Test
    void shouldPublishOnlyValidatedOrdinaryCacheEventForAllInstances() {
        var manager = new SimpleCacheManager();
        manager.setCaches(List.of(new ConcurrentMapCache("core:dept")));
        manager.initializeCaches();
        var redis = mock(RedisTemplate.class);
        var coordinator = new CacheInvalidationCoordinator(new CacheRegionRegistry(manager), redis);

        var result = coordinator.clear(Set.of("core:dept"), true, true, UUID.randomUUID());

        verify(redis).convertAndSend(CacheInvalidationCoordinator.CHANNEL, result.operationId().toString()
                + ";true;*");
        assertTrue(result.broadcastAccepted());
    }
}

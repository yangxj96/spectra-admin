/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.system.cache;

import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CacheRegionRegistryTest {

    @Test
    void shouldExposeOnlyExplicitlyRegisteredBusinessRegions() {
        var manager = new SimpleCacheManager();
        manager.setCaches(List.of(new ConcurrentMapCache("core:dept"), new ConcurrentMapCache("unregistered")));
        manager.initializeCaches();
        var registry = new CacheRegionRegistry(manager);

        assertEquals(List.of("core:dept"), registry.list().stream().map(CacheRegionDescriptor::code).toList());
        assertEquals("core:dept", registry.require("core:dept").code());
    }

    @Test
    void shouldRejectUnknownAndSecurityRegionsBeforeCacheAccess() {
        var manager = new SimpleCacheManager();
        manager.setCaches(List.of(new ConcurrentMapCache("core:dept")));
        manager.initializeCaches();
        var registry = new CacheRegionRegistry(manager);

        assertThrows(IllegalArgumentException.class, () -> registry.require("unknown"));
        assertThrows(IllegalArgumentException.class, () -> registry.require("sec:session"));
        assertThrows(IllegalArgumentException.class, () -> registry.validate(List.of("core:dept", "sec:any")));
    }
}

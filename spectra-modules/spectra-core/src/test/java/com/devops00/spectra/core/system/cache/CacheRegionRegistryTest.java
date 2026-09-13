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
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 验证 {@code CacheRegionRegistryTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
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

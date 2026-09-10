/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.system.cache;

/** 普通业务缓存区域的显式白名单元数据。 */
public record CacheRegionDescriptor(
        String code,
        String displayName,
        String provider,
        String mode,
        Long ttlSeconds,
        boolean supportsStats,
        boolean supportsClear) {
}

/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.vo;

import com.devops00.spectra.core.system.cache.CacheStatistics;

/** 普通缓存区域监控结果。 */
public record CacheRegionVO(
        String code,
        String displayName,
        String provider,
        String mode,
        Long ttlSeconds,
        boolean supportsStats,
        boolean supportsClear,
        CacheStatistics statistics) {
}

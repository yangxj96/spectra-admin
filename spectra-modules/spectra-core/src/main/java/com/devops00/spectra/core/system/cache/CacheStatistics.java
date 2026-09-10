/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.system.cache;

/** 缓存区域统计；底层不支持的指标保持 null，并由状态字段说明。 */
public record CacheStatistics(
        Long keyCount,
        Long hitCount,
        Long missCount,
        Double hitRate,
        String status) {

    /** 当前 Spring Cache 适配器未暴露精确指标时的统计结果。 */
    public static CacheStatistics unsupported() {
        return new CacheStatistics(null, null, null, null, "UNSUPPORTED");
    }
}

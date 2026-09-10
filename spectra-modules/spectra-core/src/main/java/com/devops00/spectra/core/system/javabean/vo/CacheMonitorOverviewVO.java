/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.vo;

import java.time.Instant;

/** 缓存监控总览。 */
public record CacheMonitorOverviewVO(
        String status,
        Instant generatedAt,
        int regionCount,
        Long onlineSessionCount,
        int issueCount,
        String securityRedisStatus) {
}

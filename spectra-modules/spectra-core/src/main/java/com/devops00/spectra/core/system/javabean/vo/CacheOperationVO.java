/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.vo;

import java.time.Instant;
import java.util.UUID;

/** 缓存或安全维护操作的脱敏结果。 */
public record CacheOperationVO(
        UUID operationId,
        String operationType,
        String status,
        long affectedCount,
        boolean broadcastAccepted,
        String message,
        Instant completedAt) {
}

/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.from;

import java.util.List;
import java.util.UUID;

/** 普通业务缓存清理确认请求。 */
public record CacheBusinessClearFrom(
        List<String> regionCodes,
        boolean allRegions,
        boolean allInstances,
        UUID operationId,
        String reason,
        String confirmation) {
}

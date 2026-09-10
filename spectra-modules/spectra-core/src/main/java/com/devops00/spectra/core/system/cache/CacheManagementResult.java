/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.system.cache;

import java.util.UUID;

/** 普通缓存清理操作结果。 */
public record CacheManagementResult(
        UUID operationId,
        String status,
        long affectedRegions,
        boolean broadcastAccepted,
        String message) {
}

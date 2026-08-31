/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.common.port.file;

import java.util.UUID;

/** 文件下载授权上下文。 */
public record FileAccessContext(UUID userId, String referenceType, UUID referenceId, Long rangeStart, Long rangeEnd) {

    public static FileAccessContext user(UUID userId) {
        return new FileAccessContext(userId, null, null, null, null);
    }
}

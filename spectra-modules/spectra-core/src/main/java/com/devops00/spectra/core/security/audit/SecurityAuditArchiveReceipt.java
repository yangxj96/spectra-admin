/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.audit;

import java.time.Instant;

/** 一次不可变安全审计归档对象的存储回执。 */
public record SecurityAuditArchiveReceipt(String objectUri,
                                          String contentSha256,
                                          long contentLength,
                                          Instant retainUntil) {

    public SecurityAuditArchiveReceipt {
        if (objectUri == null || objectUri.isBlank()) {
            throw new IllegalArgumentException("归档对象 URI 不能为空");
        }
        if (contentSha256 == null || !contentSha256.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("归档对象必须提供小写 SHA-256 摘要");
        }
        if (contentLength < 0) {
            throw new IllegalArgumentException("归档对象长度不能为负数");
        }
        if (retainUntil == null) {
            throw new IllegalArgumentException("归档对象必须提供保留截止时间");
        }
    }
}

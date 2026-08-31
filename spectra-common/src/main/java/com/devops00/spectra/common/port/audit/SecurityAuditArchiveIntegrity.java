/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.common.port.audit;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Locale;

/** 归档对象的完整性摘要和恢复校验。 */
public final class SecurityAuditArchiveIntegrity {

    private SecurityAuditArchiveIntegrity() {
    }

    public static String sha256(byte[] content) {
        if (content == null) {
            throw new IllegalArgumentException("归档内容不能为空");
        }
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        } catch (Exception exception) {
            throw new IllegalStateException("初始化 SHA-256 失败", exception);
        }
    }

    public static void verify(byte[] content, String expectedSha256) {
        if (!MessageDigest.isEqual(sha256(content).getBytes(StandardCharsets.US_ASCII),
                expectedSha256 == null
                        ? new byte[0]
                        : expectedSha256.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII))) {
            throw new IllegalStateException("安全审计归档完整性校验失败");
        }
    }

    /** 校验归档对象长度和摘要。 */
    public static void verify(byte[] content, String expectedSha256, long expectedLength) {
        if (expectedLength < 0 || content == null || content.length != expectedLength) {
            throw new IllegalStateException("安全审计归档长度校验失败");
        }
        verify(content, expectedSha256);
    }
}

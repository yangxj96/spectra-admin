/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.audit.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityAuditArchiveIntegrityTest {

    @Test
    void shouldVerifyAndRejectTamperedArchiveContent() {
        byte[] content = "security-audit".getBytes(StandardCharsets.UTF_8);
        String digest = SecurityAuditArchiveIntegrity.sha256(content);

        assertDoesNotThrow(() -> SecurityAuditArchiveIntegrity.verify(content, digest));
        assertThrows(IllegalStateException.class,
                () -> SecurityAuditArchiveIntegrity.verify("tampered".getBytes(StandardCharsets.UTF_8), digest));
    }
}

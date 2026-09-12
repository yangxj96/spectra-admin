/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit;

import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditContext;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.DefaultAuditSanitizer;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditRecordSanitizationTest {

    @Test
    void factorySanitizesNestedCredentialFieldsBeforeCreatingRecord() {
        var factory = new AuditRecordFactory(new DefaultAuditSanitizer());

        var record = factory.create(null, "PASSWORD_CHANGED", null, null, "WEB", null, null,
                Map.of("username", "alice", "password", "must-not-persist",
                        "nested", Map.of("refresh_token", "must-not-persist"),
                        "items", List.of(Map.of("clientSecret", "must-not-persist", "name", "safe"))),
                Map.of(), "test", Instant.EPOCH, AuditRecord.Result.STARTED, "correlation");

        assertEquals("alice", record.before().get("username"));
        assertEquals("***", record.before().get("password"));
        assertEquals("***", ((Map<?, ?>) record.before().get("nested")).get("refresh_token"));
        assertEquals("safe", ((Map<?, ?>) ((List<?>) record.before().get("items")).getFirst()).get("name"));
        assertEquals("***", ((Map<?, ?>) ((List<?>) record.before().get("items")).getFirst()).get("clientSecret"));
    }

    @Test
    void auditRecordDefensivelyPreservesLegitimateNullSnapshotValues() {
        var before = new java.util.HashMap<String, Object>();
        before.put("nickname", null);
        var record = new AuditRecord(null, AuditCategory.OPERATION, "PROFILE_UPDATED", null,
                AuditRecord.Result.SUCCEEDED, Instant.EPOCH, AuditContext.empty(), before, Map.of(), null);

        assertEquals(null, record.before().get("nickname"));
    }
}

/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.security.audit.service;

import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityAuditArchiveAuditTrailTest {

    @Test
    void archiveFailureIsRecordedAndUnknownOperationsAreRejected() {
        var auditService = new RecordingAuditService();
        var trail = new SecurityAuditArchiveAuditTrail(auditService);

        trail.append("SECURITY_AUDIT_ARCHIVE_FAILED", null, "security_audit_2025_01", "checksum mismatch");

        assertEquals("SECURITY_AUDIT_ARCHIVE_FAILED", auditService.records.getFirst().eventType());
        assertEquals(AuditRecord.Result.FAILED, auditService.records.getFirst().result());
        trail.append("SECURITY_AUDIT_ARCHIVE_STARTED", null, "security_audit_2025_02", "copy started");
        assertEquals(AuditRecord.Result.STARTED, auditService.records.get(1).result());
        assertThrows(IllegalArgumentException.class,
                () -> trail.append("SECURITY_AUDIT_ARCHIVE_DELETED", null, "security_audit_2025_01", "not allowed"));
    }

    private static final class RecordingAuditService implements AuditService {

        private final List<AuditRecord> records = new ArrayList<>();

        @Override
        public void record(AuditRecord record) {
            records.add(record);
        }
    }
}

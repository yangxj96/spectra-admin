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

/**
 * 验证 {@code AuditRecordSanitizationTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
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

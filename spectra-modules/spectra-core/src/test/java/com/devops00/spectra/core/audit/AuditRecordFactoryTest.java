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
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditSanitizer;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证 {@code AuditRecordFactoryTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class AuditRecordFactoryTest {

    @Test
    void createsSecurityCategoryAndSanitizesSnapshotsAndReason() {
        AuditSanitizer sanitizer = mock(AuditSanitizer.class);
        when(sanitizer.sanitize(any())).thenReturn(Map.of("password", "***", "reason", "safe"));
        var factory = new AuditRecordFactory(sanitizer);
        UUID eventId = UUID.randomUUID();

        AuditRecord record = factory.create(eventId, "USER_UPDATED", UUID.randomUUID(), null, "WEB", null,
                null, Map.of("password", "plain"), Map.of("password", "plain"), "unsafe reason", Instant.EPOCH,
                AuditRecord.Result.SUCCEEDED, "correlation-1");

        assertEquals(eventId, record.eventId());
        assertEquals(AuditCategory.SECURITY, record.category());
        assertEquals("***", record.before().get("password"));
        assertEquals("***", record.after().get("password"));
        assertEquals("safe", record.reason());
        verify(sanitizer, times(3)).sanitize(any());
    }
}

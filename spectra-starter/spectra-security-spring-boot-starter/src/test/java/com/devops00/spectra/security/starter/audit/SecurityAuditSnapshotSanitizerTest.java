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

package com.devops00.spectra.security.starter.audit;

import com.devops00.spectra.security.base.audit.AuditResult;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Security Audit 敏感快照回归测试。
 */
class SecurityAuditSnapshotSanitizerTest {

    @Test
    void shouldRemoveSensitiveKeysRecursivelyBeforePersistence() {
        var event = new SecurityAuditEvent(null, "PASSWORD_CHANGED", null, null, "WEB", null, null,
                Map.of("username", "alice", "password", "must-not-persist",
                        "nested", Map.of("refresh_token", "must-not-persist"),
                        "items", List.of(Map.of("clientSecret", "must-not-persist", "name", "safe"))),
                Map.of(), "test", null, AuditResult.STARTED, "corr");

        assertEquals("alice", event.before().get("username"));
        assertFalse(event.before().containsKey("password"));
        assertFalse(((Map<?, ?>) event.before().get("nested")).containsKey("refresh_token"));
        assertEquals("safe", ((Map<?, ?>) ((List<?>) event.before().get("items")).getFirst()).get("name"));
        assertFalse(((Map<?, ?>) ((List<?>) event.before().get("items")).getFirst()).containsKey("clientSecret"));
    }
}

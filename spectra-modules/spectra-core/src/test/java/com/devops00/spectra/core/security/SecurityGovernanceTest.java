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

package com.devops00.spectra.core.security;

import com.devops00.spectra.core.security.change.service.impl.DefaultSecurityChangeExecutor;
import com.devops00.spectra.core.security.root.service.impl.JdbcLastEffectiveDevOpsGuard;
import com.devops00.spectra.security.base.audit.AuditResult;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import com.devops00.spectra.security.base.audit.SecurityAuditUnavailableException;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
import com.devops00.spectra.security.base.root.RootPolicy;
import com.devops00.spectra.security.base.root.RootPolicyRepository;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 1 安全审计、事务门禁和最后 Root 保护测试。
 */
class SecurityGovernanceTest {

    @Test
    void highRiskMutationMustNotRunWhenAuditIsUnavailable() {
        var writer = new RecordingAuditWriter();
        writer.available = false;
        var executor = new DefaultSecurityChangeExecutor(writer);
        var executed = new AtomicBoolean();

        assertThrows(SecurityAuditUnavailableException.class,
                () -> executor.execute(event(), () -> {
                    executed.set(true);
                    return "unexpected";
                }));

        assertTrue(!executed.get());
        assertTrue(writer.events.isEmpty());
    }

    @Test
    void successfulMutationWritesStartedAndSucceededFacts() {
        var writer = new RecordingAuditWriter();
        var executor = new DefaultSecurityChangeExecutor(writer);

        assertEquals("ok", executor.execute(event(), () -> "ok"));
        assertEquals(List.of(AuditResult.STARTED, AuditResult.SUCCEEDED),
                writer.events.stream().map(SecurityAuditEvent::result).toList());
    }

    @Test
    void lastEffectiveRootCannotBeRemoved() {
        var writer = new RecordingAuditWriter();
        var repository = new RecordingRootPolicyRepository(1);
        var guard = new JdbcLastEffectiveDevOpsGuard(repository, writer);

        assertThrows(RuntimeException.class, guard::assertCanRemoveDevOps);
    }

    @Test
    void rootUpperBoundIsCheckedWithLockedPolicySnapshot() {
        var writer = new RecordingAuditWriter();
        var repository = new RecordingRootPolicyRepository(3);
        var guard = new JdbcLastEffectiveDevOpsGuard(repository, writer);

        assertThrows(RuntimeException.class, guard::assertCanAddDevOps);
    }

    private static SecurityAuditEvent event() {
        return new SecurityAuditEvent(UUID.randomUUID(), "DEV_OPS_TEST", null, null, "WEB", "127.0.0.1", null,
                Map.of(), Map.of(), "test", null, AuditResult.STARTED, "corr");
    }

    private static final class RecordingAuditWriter implements SecurityAuditWriter {

        private final List<SecurityAuditEvent> events = new ArrayList<>();
        private boolean available = true;

        @Override
        public void assertAvailable() {
            if (!available) {
                throw new SecurityAuditUnavailableException("audit unavailable");
            }
        }

        @Override
        public void append(SecurityAuditEvent event) {
            assertAvailable();
            events.add(event);
        }
    }

    private static final class RecordingRootPolicyRepository implements RootPolicyRepository {

        private final long current;

        private RecordingRootPolicyRepository(long current) {
            this.current = current;
        }

        @Override
        public RootPolicy lock() {
            return RootPolicy.defaults();
        }

        @Override
        public void update(RootPolicy policy, long expectedVersion) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long countEffectiveDevOpsUsers() {
            return current;
        }
    }
}

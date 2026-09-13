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
import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditContext;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.audit.DefaultAuditSanitizer;
import com.devops00.spectra.core.audit.AuditFailureResolver;
import com.devops00.spectra.core.audit.AuditFailureRecorder;
import com.devops00.spectra.core.security.root.service.impl.JdbcLastEffectiveDevOpsGuard;
import com.devops00.spectra.core.security.root.RootPolicy;
import com.devops00.spectra.core.security.root.RootPolicyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.Instant;
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
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class SecurityGovernanceTest {

    @Test
    void highRiskMutationMustNotRunWhenAuditIsUnavailable() {
        var writer = new RecordingAuditService();
        writer.available = false;
        var executor = executor(writer, new ArrayList<>());
        var executed = new AtomicBoolean();

        assertThrows(AuditService.AuditRecordingException.class,
                () -> executor.execute(event(), () -> {
                    executed.set(true);
                    return "unexpected";
                }));

        assertTrue(!executed.get());
        assertTrue(writer.events.isEmpty());
    }

    @Test
    void successfulMutationWritesStartedAndSucceededFacts() {
        var writer = new RecordingAuditService();
        var executor = executor(writer, new ArrayList<>());

        assertEquals("ok", executor.execute(event(), () -> "ok"));
        assertEquals(List.of(AuditRecord.Result.STARTED, AuditRecord.Result.SUCCEEDED),
                writer.events.stream().map(AuditRecord::result).toList());
    }

    @Test
    void lastEffectiveRootCannotBeRemoved() {
        var writer = new RecordingAuditService();
        var repository = new RecordingRootPolicyRepository(1);
        var guard = new JdbcLastEffectiveDevOpsGuard(repository, writer);

        assertThrows(RuntimeException.class, guard::assertCanRemoveDevOps);
    }

    @Test
    void rootUpperBoundIsCheckedWithLockedPolicySnapshot() {
        var writer = new RecordingAuditService();
        var repository = new RecordingRootPolicyRepository(3);
        var guard = new JdbcLastEffectiveDevOpsGuard(repository, writer);

        assertThrows(RuntimeException.class, guard::assertCanAddDevOps);
    }

    /**
     * 处理安全相关数据。
     */
    private static DefaultSecurityChangeExecutor executor(RecordingAuditService service, List<AuditRecord> failures) {
        var failureRecorder = new AuditFailureRecorder(failures::add, new NoopTransactionManager());
        return new DefaultSecurityChangeExecutor(service, new AuditFailureResolver(new DefaultAuditSanitizer()),
                failureRecorder);
    }

    /**
     * 处理事件相关数据。
     */
    private static AuditRecord event() {
        return new AuditRecord(UUID.randomUUID(), AuditCategory.SECURITY, "DEV_OPS_TEST", null,
                AuditRecord.Result.STARTED, Instant.now(), AuditContext.empty(), Map.of(), Map.of(), "test");
    }

    /**
     * 为 {@code SecurityGovernanceTest} 测试提供 {@code RecordingAuditService} 测试类型。
     *
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    private static final class RecordingAuditService implements AuditService {

        private final List<AuditRecord> events = new ArrayList<>();
        private boolean available = true;

        @Override
        public void record(AuditRecord event) {
            if (!available) {
                throw new AuditService.AuditRecordingException("audit unavailable");
            }
            events.add(event);
        }

        @Override
        public void assertAvailable() {
            if (!available) {
                throw new AuditService.AuditRecordingException("audit unavailable");
            }
        }
    }

    /**
     * 为 {@code SecurityGovernanceTest} 测试提供 {@code NoopTransactionManager} 测试类型。
     *
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    private static final class NoopTransactionManager implements PlatformTransactionManager {

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
        }

        @Override
        public void rollback(TransactionStatus status) {
        }
    }

    /**
     * 为 {@code SecurityGovernanceTest} 测试提供 {@code RecordingRootPolicyRepository} 测试类型。
     *
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
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

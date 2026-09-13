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
import com.devops00.spectra.common.audit.AuditService;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * 验证 {@code AuditFailureRecorderTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class AuditFailureRecorderTest {

    @Test
    void failureMustBeRecordedInRequiresNewTransaction() {
        AtomicReference<AuditRecord> received = new AtomicReference<>();
        var transactionManager = new RecordingTransactionManager();
        AuditService service = received::set;
        var recorder = new AuditFailureRecorder(service, transactionManager);
        var record = new AuditRecord(UUID.randomUUID(), AuditCategory.OPERATION, "TEST.FAILED", null,
                AuditRecord.Result.FAILED, Instant.now(), AuditContext.empty(), Map.of(), Map.of(), null);

        recorder.record(record);

        assertSame(record, received.get());
        assertEquals(TransactionDefinition.PROPAGATION_REQUIRES_NEW, transactionManager.propagation);
        assertEquals(1, transactionManager.commits);
        assertEquals(0, transactionManager.rollbacks);
    }

    /**
     * 为 {@code AuditFailureRecorderTest} 测试提供 {@code RecordingTransactionManager} 测试类型。
     *
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    private static final class RecordingTransactionManager implements PlatformTransactionManager {

        private int propagation = TransactionDefinition.PROPAGATION_REQUIRED;
        private int commits;
        private int rollbacks;

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            propagation = definition.getPropagationBehavior();
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
            commits++;
        }

        @Override
        public void rollback(TransactionStatus status) {
            rollbacks++;
        }
    }
}

/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
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

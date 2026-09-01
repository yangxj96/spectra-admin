/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.audit.archive;

import com.devops00.spectra.common.port.audit.SecurityAuditArchiveBackend;
import com.devops00.spectra.common.port.audit.SecurityAuditArchiveIntegrity;
import com.devops00.spectra.common.port.audit.SecurityAuditArchiveReceipt;
import com.devops00.spectra.core.security.audit.service.SecurityAuditArchiveAuditTrail;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityAuditArchiveWorkerTest {

    private static final Instant NOW = Instant.parse("2026-08-31T04:05:06Z");
    private static final UUID MANIFEST_ID = UUID.fromString("018f0a6f-3b6f-7b2f-8e4f-4e6a7d9c1234");
    private static final UUID OPERATOR_ID = UUID.fromString("018f0a6f-3b6f-7b2f-8e4f-4e6a7d9c5678");
    private static final String PARTITION = "sec_security_audit_event_2026_08";
    private static final Instant RANGE_START = Instant.parse("2026-08-01T00:00:00Z");
    private static final Instant RANGE_END = Instant.parse("2026-09-01T00:00:00Z");
    private static final byte[] CONTENT = "{\"event_id\":\"one\"}\n".getBytes(StandardCharsets.UTF_8);

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    @AfterEach
    void closeMeterRegistry() {
        meterRegistry.close();
    }

    @Test
    void writesArchiveAndConfirmsManifestAfterObjectReceipt() {
        var repository = mock(SecurityAuditArchiveManifestRepository.class);
        var dataRepository = mock(SecurityAuditArchiveDataRepository.class);
        var backend = mock(SecurityAuditArchiveBackend.class);
        var backendProvider = provider(backend);
        var auditTrail = mock(SecurityAuditArchiveAuditTrail.class);
        var manifest = manifest("PLANNED", null, null, null, null);
        var snapshot = new SecurityAuditArchiveDataRepository.ArchiveSnapshot(CONTENT, 1L);
        String digest = SecurityAuditArchiveIntegrity.sha256(CONTENT);
        when(backend.id()).thenReturn("S3_OBJECT_LOCK");
        when(repository.claimBatch("worker-a", NOW, NOW.plusSeconds(60), 20, 10)).thenReturn(List.of(manifest));
        when(repository.totalRetentionYears()).thenReturn(5);
        when(repository.archiveBackend()).thenReturn("S3_OBJECT_LOCK");
        when(dataRepository.snapshot(PARTITION, RANGE_START, RANGE_END)).thenReturn(snapshot);
        when(backend.put(eq(PARTITION + "/" + MANIFEST_ID + ".jsonl"), any(byte[].class), any(Instant.class)))
                .thenReturn(new SecurityAuditArchiveReceipt("s3://archive/security-audit/object.jsonl", digest,
                        CONTENT.length, NOW.plusSeconds(3600)));
        when(repository.markArchived(eq(MANIFEST_ID), eq("worker-a"), anyString(), eq(digest),
                anyLong(), eq(1L), any(Instant.class), eq(OPERATOR_ID), any(Instant.class))).thenReturn(1);
        when(repository.pendingCount()).thenReturn(0L);
        when(repository.oldestPendingAt()).thenReturn(null);

        var worker = worker(repository, dataRepository, backendProvider, auditTrail, "worker-a");

        assertEquals(new SecurityAuditArchiveWorker.BatchResult(1, 1, 0, 0), worker.processBatch());
        verify(backend).put(eq(PARTITION + "/" + MANIFEST_ID + ".jsonl"), any(byte[].class), any(Instant.class));
        verify(auditTrail).append("SECURITY_AUDIT_ARCHIVE_STARTED", OPERATOR_ID, PARTITION,
                "manifestId=" + MANIFEST_ID);
        verify(auditTrail).append("SECURITY_AUDIT_ARCHIVE_COMPLETED", OPERATOR_ID, PARTITION,
                "manifestId=" + MANIFEST_ID + ";rows=1");
    }

    @Test
    void keepsManifestRetryableWhenObjectStorageUploadFails() {
        var repository = mock(SecurityAuditArchiveManifestRepository.class);
        var dataRepository = mock(SecurityAuditArchiveDataRepository.class);
        var backend = mock(SecurityAuditArchiveBackend.class);
        var auditTrail = mock(SecurityAuditArchiveAuditTrail.class);
        var manifest = manifest("PLANNED", null, null, null, null);
        when(backend.id()).thenReturn("S3_OBJECT_LOCK");
        when(repository.claimBatch("worker-a", NOW, NOW.plusSeconds(60), 20, 10)).thenReturn(List.of(manifest));
        when(repository.archiveBackend()).thenReturn("S3_OBJECT_LOCK");
        when(repository.totalRetentionYears()).thenReturn(5);
        when(dataRepository.snapshot(PARTITION, RANGE_START, RANGE_END))
                .thenReturn(new SecurityAuditArchiveDataRepository.ArchiveSnapshot(CONTENT, 1L));
        doThrow(new IllegalStateException("object storage unavailable")).when(backend)
                .put(anyString(), any(byte[].class), any(Instant.class));
        when(repository.markFailed(eq(MANIFEST_ID), eq("worker-a"), eq(OPERATOR_ID), eq(NOW),
                any(Instant.class), eq("IllegalStateException: object storage unavailable"))).thenReturn(1);
        when(repository.pendingCount()).thenReturn(1L);
        when(repository.oldestPendingAt()).thenReturn(NOW);

        var worker = worker(repository, dataRepository, provider(backend), auditTrail, "worker-a");

        assertEquals(new SecurityAuditArchiveWorker.BatchResult(1, 0, 1, 1), worker.processBatch());
        verify(repository).markFailed(eq(MANIFEST_ID), eq("worker-a"), eq(OPERATOR_ID), eq(NOW),
                any(Instant.class), eq("IllegalStateException: object storage unavailable"));
        verify(repository, org.mockito.Mockito.never()).markArchived(any(), any(), anyString(), anyString(),
                anyLong(), anyLong(), any(), any(), any());
        verify(auditTrail).append("SECURITY_AUDIT_ARCHIVE_STARTED", OPERATOR_ID, PARTITION,
                "manifestId=" + MANIFEST_ID);
        verify(auditTrail).append("SECURITY_AUDIT_ARCHIVE_FAILED", OPERATOR_ID, PARTITION,
                "manifestId=" + MANIFEST_ID + ";error=IllegalStateException: object storage unavailable");
    }

    @Test
    void verifiesObjectSourceRangeAndRowCountBeforeVerifiedState() {
        var repository = mock(SecurityAuditArchiveManifestRepository.class);
        var dataRepository = mock(SecurityAuditArchiveDataRepository.class);
        var backend = mock(SecurityAuditArchiveBackend.class);
        var auditTrail = mock(SecurityAuditArchiveAuditTrail.class);
        String digest = SecurityAuditArchiveIntegrity.sha256(CONTENT);
        var manifest = manifest("ARCHIVED", "s3://archive/object.jsonl", digest, (long) CONTENT.length, 1L);
        when(backend.id()).thenReturn("S3_OBJECT_LOCK");
        when(repository.claimBatch("worker-a", NOW, NOW.plusSeconds(60), 20, 10)).thenReturn(List.of(manifest));
        when(repository.archiveBackend()).thenReturn("S3_OBJECT_LOCK");
        when(backend.exists(manifest.objectUri())).thenReturn(true);
        when(dataRepository.snapshot(PARTITION, RANGE_START, RANGE_END))
                .thenReturn(new SecurityAuditArchiveDataRepository.ArchiveSnapshot(CONTENT, 1L));
        when(repository.markVerified(MANIFEST_ID, "worker-a", OPERATOR_ID, NOW)).thenReturn(1);
        when(repository.pendingCount()).thenReturn(0L);
        when(repository.oldestPendingAt()).thenReturn(null);

        var worker = worker(repository, dataRepository, provider(backend), auditTrail, "worker-a");

        assertEquals(1, worker.processBatch().processed());
        verify(backend).verify(manifest.objectUri(), digest, CONTENT.length);
        verify(repository).markVerified(MANIFEST_ID, "worker-a", OPERATOR_ID, NOW);
        verify(auditTrail).append("SECURITY_AUDIT_ARCHIVE_VERIFIED", OPERATOR_ID, PARTITION,
                "manifestId=" + MANIFEST_ID + ";rows=1");
    }

    @Test
    void movesTamperedObjectToFailureWithoutDeletingSource() {
        var repository = mock(SecurityAuditArchiveManifestRepository.class);
        var dataRepository = mock(SecurityAuditArchiveDataRepository.class);
        var backend = mock(SecurityAuditArchiveBackend.class);
        var auditTrail = mock(SecurityAuditArchiveAuditTrail.class);
        String digest = SecurityAuditArchiveIntegrity.sha256(CONTENT);
        var manifest = manifest("ARCHIVED", "s3://archive/object.jsonl", digest, (long) CONTENT.length, 1L);
        when(backend.id()).thenReturn("S3_OBJECT_LOCK");
        when(repository.claimBatch("worker-a", NOW, NOW.plusSeconds(60), 20, 10)).thenReturn(List.of(manifest));
        when(repository.archiveBackend()).thenReturn("S3_OBJECT_LOCK");
        when(backend.exists(manifest.objectUri())).thenReturn(true);
        doThrow(new IllegalStateException("checksum mismatch")).when(backend)
                .verify(manifest.objectUri(), digest, CONTENT.length);
        when(repository.markFailed(eq(MANIFEST_ID), eq("worker-a"), eq(OPERATOR_ID), eq(NOW),
                any(Instant.class), eq("IllegalStateException: checksum mismatch"))).thenReturn(1);
        when(repository.pendingCount()).thenReturn(1L);
        when(repository.oldestPendingAt()).thenReturn(NOW);

        var worker = worker(repository, dataRepository, provider(backend), auditTrail, "worker-a");

        assertEquals(new SecurityAuditArchiveWorker.BatchResult(1, 0, 1, 1), worker.processBatch());
        verify(repository).markFailed(eq(MANIFEST_ID), eq("worker-a"), eq(OPERATOR_ID), eq(NOW),
                any(Instant.class), eq("IllegalStateException: checksum mismatch"));
        verify(dataRepository, org.mockito.Mockito.never()).snapshot(anyString(), any(), any());
        verify(repository, org.mockito.Mockito.never()).markVerified(any(), any(), any(), any());
    }

    @Test
    void missingBackendFailsClosedAndKeepsManifestForLaterRetry() {
        var repository = mock(SecurityAuditArchiveManifestRepository.class);
        var dataRepository = mock(SecurityAuditArchiveDataRepository.class);
        var auditTrail = mock(SecurityAuditArchiveAuditTrail.class);
        var manifest = manifest("PLANNED", null, null, null, null);
        when(repository.claimBatch("worker-a", NOW, NOW.plusSeconds(60), 20, 10)).thenReturn(List.of(manifest));
        when(repository.archiveBackend()).thenReturn("S3_OBJECT_LOCK");
        when(repository.markFailed(eq(MANIFEST_ID), eq("worker-a"), eq(OPERATOR_ID), eq(NOW),
                any(Instant.class), eq("IllegalStateException: 安全审计归档 backend 未启用"))).thenReturn(1);
        when(repository.pendingCount()).thenReturn(1L);
        when(repository.oldestPendingAt()).thenReturn(NOW);

        var worker = worker(repository, dataRepository, provider(null), auditTrail, "worker-a");

        assertEquals(1, worker.processBatch().failed());
        verify(repository).markFailed(eq(MANIFEST_ID), eq("worker-a"), eq(OPERATOR_ID), eq(NOW),
                any(Instant.class), eq("IllegalStateException: 安全审计归档 backend 未启用"));
    }

    @Test
    void restoresOnlyAfterVerifiedObjectAndSourceRangeMatch() {
        var repository = mock(SecurityAuditArchiveManifestRepository.class);
        var dataRepository = mock(SecurityAuditArchiveDataRepository.class);
        var backend = mock(SecurityAuditArchiveBackend.class);
        var auditTrail = mock(SecurityAuditArchiveAuditTrail.class);
        String digest = SecurityAuditArchiveIntegrity.sha256(CONTENT);
        var manifest = manifest("RESTORE_PENDING", "s3://archive/object.jsonl", digest, (long) CONTENT.length, 1L);
        when(backend.id()).thenReturn("S3_OBJECT_LOCK");
        when(repository.claimBatch("worker-a", NOW, NOW.plusSeconds(60), 20, 10)).thenReturn(List.of(manifest));
        when(repository.archiveBackend()).thenReturn("S3_OBJECT_LOCK");
        when(backend.exists(manifest.objectUri())).thenReturn(true);
        when(dataRepository.snapshot(PARTITION, RANGE_START, RANGE_END))
                .thenReturn(new SecurityAuditArchiveDataRepository.ArchiveSnapshot(CONTENT, 1L));
        when(repository.markRestored(MANIFEST_ID, "worker-a", OPERATOR_ID, NOW)).thenReturn(1);
        when(repository.pendingCount()).thenReturn(0L);
        when(repository.oldestPendingAt()).thenReturn(null);

        var worker = worker(repository, dataRepository, provider(backend), auditTrail, "worker-a");

        assertEquals(1, worker.processBatch().processed());
        verify(repository).markRestored(MANIFEST_ID, "worker-a", OPERATOR_ID, NOW);
        verify(auditTrail).append("SECURITY_AUDIT_ARCHIVE_RESTORED", OPERATOR_ID, PARTITION,
                "manifestId=" + MANIFEST_ID + ";rows=1");
    }

    private SecurityAuditArchiveWorker worker(SecurityAuditArchiveManifestRepository repository,
                                              SecurityAuditArchiveDataRepository dataRepository,
                                              ObjectProvider<SecurityAuditArchiveBackend> backendProvider,
                                              SecurityAuditArchiveAuditTrail auditTrail,
                                              String owner) {
        return new SecurityAuditArchiveWorker(repository, dataRepository, backendProvider, auditTrail,
                transactionTemplate(), meterRegistry, Clock.fixed(NOW, ZoneOffset.UTC), owner);
    }

    private static ObjectProvider<SecurityAuditArchiveBackend> provider(SecurityAuditArchiveBackend backend) {
        @SuppressWarnings("unchecked")
        ObjectProvider<SecurityAuditArchiveBackend> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(backend);
        return provider;
    }

    private static TransactionTemplate transactionTemplate() {
        TransactionTemplate template = mock(TransactionTemplate.class);
        when(template.execute(any(TransactionCallback.class))).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(new SimpleTransactionStatus());
        });
        doAnswer(invocation -> {
            Consumer<?> action = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            Consumer<org.springframework.transaction.TransactionStatus> transactionAction = (Consumer<org.springframework.transaction.TransactionStatus>) action;
            transactionAction.accept(new SimpleTransactionStatus());
            return null;
        }).when(template).executeWithoutResult(any());
        return template;
    }

    private static SecurityAuditArchiveManifestRepository.ArchiveManifest manifest(
                                                                                   String state, String objectUri, String digest, Long contentLength,
                                                                                   Long rowCount) {
        return new SecurityAuditArchiveManifestRepository.ArchiveManifest(
                MANIFEST_ID, MANIFEST_ID, PARTITION, RANGE_START, RANGE_END, objectUri, digest, contentLength, rowCount,
                state, "ARCHIVED".equals(state) || "RESTORE_PENDING".equals(state) ? NOW : null,
                "VERIFIED".equals(state) ? NOW : null, null, 1, NOW, "worker-a", NOW.plusSeconds(60), OPERATOR_ID,
                NOW.minusSeconds(60), OPERATOR_ID, NOW);
    }
}

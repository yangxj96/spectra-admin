/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.service;

import com.devops00.spectra.upload.configure.FileStorageProviderRegistry;
import com.devops00.spectra.upload.javabean.constant.FileAssetStatus;
import com.devops00.spectra.upload.javabean.constant.StorageProviderType;
import com.devops00.spectra.upload.javabean.constant.TransportMode;
import com.devops00.spectra.upload.javabean.constant.UploadSessionStatus;
import com.devops00.spectra.upload.javabean.entity.FileAsset;
import com.devops00.spectra.upload.javabean.entity.FileUploadSession;
import com.devops00.spectra.upload.properties.FileUploadProperties;
import com.devops00.spectra.upload.storage.FileStorageProvider;
import com.devops00.spectra.upload.storage.StorageMultipart;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileUploadCleanupServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-31T00:00:00Z");

    @Test
    void cleansExpiredS3SessionOutsideTheSchedulerAdapterAndReportsSummary() {
        var transactionService = mock(FileUploadCleanupTransactionService.class);
        var registry = mock(FileStorageProviderRegistry.class);
        var provider = mock(FileStorageProvider.class);
        var session = session(StorageProviderType.S3);
        when(transactionService.claimExpiredSessions(any(), any(), any(), eq(100))).thenReturn(List.of(session));
        when(transactionService.claimSessionCleanupCandidates(any(), any(), eq(100))).thenReturn(List.of());
        when(transactionService.markOrphans(any(), any(), eq(100))).thenReturn(0);
        when(transactionService.claimAssetCleanupCandidates(any(), any(), eq(100))).thenReturn(List.of());
        when(registry.require(StorageProviderType.S3)).thenReturn(provider);
        var cleanupService = new FileUploadCleanupService(new FileUploadProperties(), transactionService, registry);

        var result = cleanupService.cleanupBatch(NOW);

        verify(provider).abortMultipart(new StorageMultipart("bucket", "staging/key", "provider-upload-id"));
        assertEquals(1L, result.expiredSessions());
        assertEquals(0L, result.sessionRetryScheduled());
    }

    @Test
    void preservesSessionAndSchedulesRetryWhenProviderAbortFails() {
        var transactionService = mock(FileUploadCleanupTransactionService.class);
        var registry = mock(FileStorageProviderRegistry.class);
        var provider = mock(FileStorageProvider.class);
        var session = session(StorageProviderType.LOCAL);
        when(transactionService.claimExpiredSessions(any(), any(), any(), eq(100))).thenReturn(List.of(session));
        when(transactionService.claimSessionCleanupCandidates(any(), any(), eq(100))).thenReturn(List.of());
        when(transactionService.markOrphans(any(), any(), eq(100))).thenReturn(0);
        when(transactionService.claimAssetCleanupCandidates(any(), any(), eq(100))).thenReturn(List.of());
        when(registry.require(StorageProviderType.LOCAL)).thenReturn(provider);
        doThrow(new IllegalStateException("storage unavailable")).when(provider).abortMultipart(any());
        var cleanupService = new FileUploadCleanupService(new FileUploadProperties(), transactionService, registry);

        var result = cleanupService.cleanupBatch(NOW);

        verify(transactionService).scheduleSessionRetry(eq(session.getId()), eq(NOW.plus(Duration.ofMinutes(5))));
        assertEquals(1L, result.sessionRetryScheduled());
    }

    @Test
    void finalizesAssetOnlyAfterProviderDeleteAndRetriesDeletingAssetOnFailure() {
        var transactionService = mock(FileUploadCleanupTransactionService.class);
        var registry = mock(FileStorageProviderRegistry.class);
        var provider = mock(FileStorageProvider.class);
        var success = asset(StorageProviderType.LOCAL);
        var failed = asset(StorageProviderType.S3);
        when(transactionService.claimExpiredSessions(any(), any(), any(), eq(100))).thenReturn(List.of());
        when(transactionService.claimSessionCleanupCandidates(any(), any(), eq(100))).thenReturn(List.of());
        when(transactionService.markOrphans(any(), any(), eq(100))).thenReturn(2);
        when(transactionService.claimAssetCleanupCandidates(any(), any(), eq(100))).thenReturn(List.of(success, failed));
        when(registry.require(StorageProviderType.LOCAL)).thenReturn(provider);
        when(registry.require(StorageProviderType.S3)).thenReturn(provider);
        doThrow(new IllegalStateException("delete unavailable"))
                .when(provider)
                .delete("bucket", "asset-s3");
        var cleanupService = new FileUploadCleanupService(new FileUploadProperties(), transactionService, registry);

        var result = cleanupService.cleanupBatch(NOW);

        verify(transactionService).finishAsset(success.getId());
        verify(transactionService).scheduleAssetRetry(eq(failed.getId()), eq(NOW.plus(Duration.ofMinutes(5))));
        assertEquals(2L, result.orphanedAssets());
        assertEquals(1L, result.deletedAssets());
        assertEquals(1L, result.assetRetryScheduled());
    }

    private static FileUploadSession session(StorageProviderType provider) {
        var session = new FileUploadSession();
        session.setId(UUID.randomUUID());
        session.setStatus(UploadSessionStatus.EXPIRED);
        session.setStorageProvider(provider);
        session.setTransportMode(provider == StorageProviderType.S3 ? TransportMode.PRESIGNED : TransportMode.LOCAL_PROXY);
        session.setStorageContainer("bucket");
        session.setStagingKey("staging/key");
        session.setProviderUploadId("provider-upload-id");
        return session;
    }

    private static FileAsset asset(StorageProviderType provider) {
        var asset = new FileAsset();
        asset.setId(UUID.randomUUID());
        asset.setStatus(FileAssetStatus.DELETING);
        asset.setStorageProvider(provider);
        asset.setStorageContainer("bucket");
        asset.setStorageKey(provider == StorageProviderType.S3 ? "asset-s3" : "asset-local");
        return asset;
    }
}

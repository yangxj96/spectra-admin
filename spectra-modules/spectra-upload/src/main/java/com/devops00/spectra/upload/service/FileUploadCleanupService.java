/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.service;

import com.devops00.spectra.upload.configure.FileStorageProviderRegistry;
import com.devops00.spectra.upload.javabean.entity.FileUploadSession;
import com.devops00.spectra.upload.properties.FileUploadProperties;
import com.devops00.spectra.upload.storage.StorageMultipart;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * 文件上传清理编排；数据库 claim/finalize 与 Local/S3 I/O 分离。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
@Slf4j
@Service
public class FileUploadCleanupService {

    private static final int BATCH_SIZE = 100;
    private static final Duration RETRY_DELAY = Duration.ofMinutes(5);
    private static final Duration CLAIM_LEASE = Duration.ofMinutes(15);

    private final FileUploadProperties properties;
    private final FileUploadCleanupTransactionService transactionService;
    private final FileStorageProviderRegistry providerRegistry;

    public FileUploadCleanupService(FileUploadProperties properties,
                                    FileUploadCleanupTransactionService transactionService,
                                    FileStorageProviderRegistry providerRegistry) {
        this.properties = properties;
        this.transactionService = transactionService;
        this.providerRegistry = providerRegistry;
    }

    /** 执行一个有界清理扫描；方法本身不持有数据库事务。 */
    public FileUploadCleanupSummary cleanupBatch(Instant now) {
        if (now == null) {
            throw new IllegalArgumentException("清理扫描时间不能为空");
        }
        long expiredSessions = 0;
        long sessionRetryScheduled = 0;
        long cleanedSessions = 0;
        long orphanedAssets = transactionService.markOrphans(
                now.minus(properties.getOrphanRetention()), now, BATCH_SIZE);
        long deletedAssets = 0;
        long assetRetryScheduled = 0;

        for (var session : transactionService.claimExpiredSessions(
                now, now.minus(properties.getIdleTimeout()), now.plus(properties.getRecordRetention()), BATCH_SIZE)) {
            try {
                abort(session);
                expiredSessions++;
            } catch (RuntimeException exception) {
                scheduleSessionRetry(session, now, exception);
                sessionRetryScheduled++;
            }
        }

        for (var session : transactionService.claimSessionCleanupCandidates(
                now, now.plus(CLAIM_LEASE), BATCH_SIZE)) {
            try {
                abort(session);
                transactionService.finishSession(session.getId(), now);
                cleanedSessions++;
            } catch (RuntimeException exception) {
                scheduleSessionRetry(session, now, exception);
                sessionRetryScheduled++;
            }
        }

        for (var asset : transactionService.claimAssetCleanupCandidates(
                now, now.plus(CLAIM_LEASE), BATCH_SIZE)) {
            try {
                providerRegistry.require(asset.getStorageProvider())
                        .delete(asset.getStorageContainer(), asset.getStorageKey());
                transactionService.finishAsset(asset.getId());
                deletedAssets++;
            } catch (RuntimeException exception) {
                transactionService.scheduleAssetRetry(asset.getId(), now.plus(RETRY_DELAY));
                assetRetryScheduled++;
                log.warn("文件资产清理失败，将安排重试: assetId={}", asset.getId(), exception);
            }
        }

        return new FileUploadCleanupSummary(expiredSessions, sessionRetryScheduled, cleanedSessions,
                orphanedAssets, deletedAssets, assetRetryScheduled);
    }

    private void abort(FileUploadSession session) {
        providerRegistry.require(session.getStorageProvider())
                .abortMultipart(
                        new StorageMultipart(session.getStorageContainer(), session.getStagingKey(), session.getProviderUploadId()));
    }

    private void scheduleSessionRetry(FileUploadSession session, Instant now, RuntimeException exception) {
        transactionService.scheduleSessionRetry(session.getId(), now.plus(RETRY_DELAY));
        log.warn("上传会话清理失败，将安排重试: uploadId={}", session.getId(), exception);
    }
}

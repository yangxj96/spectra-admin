/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.service;

import com.devops00.spectra.core.upload.javabean.constant.FileAssetStatus;
import com.devops00.spectra.core.upload.javabean.entity.FileAsset;
import com.devops00.spectra.core.upload.javabean.entity.FileUploadSession;
import com.devops00.spectra.core.upload.mapper.FileAssetMapper;
import com.devops00.spectra.core.upload.mapper.FileUploadSessionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传清理的短数据库事务边界；不执行任何 Local/S3 I/O。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
@Service
@RequiredArgsConstructor
public class FileUploadCleanupTransactionService {

    private final FileUploadSessionMapper sessionMapper;
    private final FileAssetMapper assetMapper;

    /** 标记一批已过期会话；事务提交后才允许调用存储提供方。 */
    @Transactional
    public List<FileUploadSession> claimExpiredSessions(Instant now, Instant idleBefore,
                                                        Instant nextCleanupAt, int limit) {
        return sessionMapper.findExpiredCandidates(now, idleBefore, limit)
                .stream()
                .filter(session -> sessionMapper.markExpired(session.getId(), nextCleanupAt) == 1)
                .toList();
    }

    /** 抢占到期会话清理记录，使用 next_cleanup_at 作为崩溃可恢复的短租约。 */
    @Transactional
    public List<FileUploadSession> claimSessionCleanupCandidates(Instant now, Instant claimUntil, int limit) {
        return sessionMapper.findCleanupCandidates(now, limit)
                .stream()
                .filter(session -> sessionMapper.claimCleanupCandidate(session.getId(), now, claimUntil) == 1)
                .toList();
    }

    /** 标记没有业务引用的 READY 资产进入孤儿保留流程。 */
    @Transactional
    public int markOrphans(Instant cutoff, Instant nextCleanupAt, int limit) {
        int marked = 0;
        for (var asset : assetMapper.findOrphanCandidates(cutoff, limit)) {
            marked += assetMapper.markOrphaned(asset.getId(), nextCleanupAt);
        }
        return marked;
    }

    /** 抢占资产删除记录；READY 进入 DELETING，已在 DELETING 的记录可恢复处理。 */
    @Transactional
    public List<FileAsset> claimAssetCleanupCandidates(Instant now, Instant claimUntil, int limit) {
        return assetMapper.findCleanupCandidates(now, limit)
                .stream()
                .filter(asset -> asset.getStatus() == FileAssetStatus.DELETING
                        || assetMapper.markDeleting(asset.getId()) == 1)
                .filter(asset -> assetMapper.claimCleanupCandidate(asset.getId(), now, claimUntil) == 1)
                .toList();
    }

    /** 安排会话清理重试，保留失败会话记录。 */
    @Transactional
    public void scheduleSessionRetry(UUID id, Instant nextCleanupAt) {
        sessionMapper.markCleanupRetry(id, nextCleanupAt);
    }

    /** 完成会话清理并在同一个短事务中删除分片和会话记录。 */
    @Transactional
    public void finishSession(UUID id, Instant now) {
        sessionMapper.markCleaned(id, now);
        sessionMapper.deletePartsPhysically(id);
        sessionMapper.deleteByIdPhysically(id, now);
    }

    /** 安排资产删除重试，保留 DELETING 状态以便下次恢复。 */
    @Transactional
    public void scheduleAssetRetry(UUID id, Instant nextCleanupAt) {
        assetMapper.markCleanupRetry(id, nextCleanupAt);
    }

    /** 完成资产删除状态机并物理删除资产记录。 */
    @Transactional
    public void finishAsset(UUID id) {
        assetMapper.markDeleted(id);
        assetMapper.deleteByIdPhysically(id);
    }
}

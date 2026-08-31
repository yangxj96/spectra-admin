/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.audit.archive;

import com.devops00.spectra.common.exception.DataExistException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.security.audit.service.SecurityAuditArchiveAuditTrail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.UUID;

/** 安全审计归档计划和人工处置的应用编排器。 */
@Service
public class SecurityAuditArchiveOrchestrator {

    private static final String PARTITION_PATTERN = "[A-Za-z0-9_]{1,128}";

    private final SecurityAuditArchiveManifestRepository manifestRepository;
    private final SecurityAuditArchiveAuditTrail auditTrail;
    private final Clock clock;

    @Autowired
    public SecurityAuditArchiveOrchestrator(SecurityAuditArchiveManifestRepository manifestRepository,
                                            SecurityAuditArchiveAuditTrail auditTrail) {
        this(manifestRepository, auditTrail, Clock.systemUTC());
    }

    SecurityAuditArchiveOrchestrator(SecurityAuditArchiveManifestRepository manifestRepository,
                                     SecurityAuditArchiveAuditTrail auditTrail,
                                     Clock clock) {
        this.manifestRepository = manifestRepository;
        this.auditTrail = auditTrail;
        this.clock = clock;
    }

    /** 为一个审计分区创建唯一归档计划；同一分区不得重复计划。 */
    @Transactional
    public ManifestView plan(String partitionName,
                             Instant rangeStart,
                             Instant rangeEnd,
                             UUID operatorId) {
        requirePartition(partitionName);
        requireRange(rangeStart, rangeEnd);
        UUID manifestId = UUID.randomUUID();
        Instant now = clock.instant();
        if (manifestRepository.createPlan(manifestId, partitionName, rangeStart, rangeEnd, operatorId, now) != 1) {
            throw new DataExistException("安全审计分区已有归档计划");
        }
        auditTrail.append("SECURITY_AUDIT_ARCHIVE_PLANNED", operatorId, partitionName,
                "rangeStart=" + rangeStart + ";rangeEnd=" + rangeEnd);
        return view(requireManifest(manifestId));
    }

    /** 查询归档 manifest 当前状态。 */
    public ManifestView get(UUID manifestId) {
        return view(requireManifest(manifestId));
    }

    /** 将失败计划清除旧对象元数据并重新放回 PLANNED。 */
    @Transactional
    public ManifestView retryFailed(UUID manifestId, UUID operatorId) {
        var manifest = requireManifest(manifestId);
        if (!"FAILED".equals(manifest.state())) {
            throw new IllegalStateException("只有 FAILED 归档计划可以重试");
        }
        if (manifestRepository.replayFailed(manifestId, operatorId, clock.instant()) != 1) {
            throw new IllegalStateException("归档计划状态已变化，重试未执行");
        }
        auditTrail.append("SECURITY_AUDIT_ARCHIVE_RETRY_REQUESTED", operatorId, manifest.partitionName(),
                "manifestId=" + manifestId);
        return view(requireManifest(manifestId));
    }

    /** 仅允许 VERIFIED 计划进入恢复校验状态；不会删除或修改源审计事实。 */
    @Transactional
    public ManifestView requestRestore(UUID manifestId, UUID operatorId) {
        var manifest = requireManifest(manifestId);
        if (!"VERIFIED".equals(manifest.state())) {
            throw new IllegalStateException("只有 VERIFIED 归档计划可以申请恢复");
        }
        if (manifestRepository.requestRestore(manifestId, operatorId, clock.instant()) != 1) {
            throw new IllegalStateException("归档计划状态已变化，恢复申请未执行");
        }
        auditTrail.append("SECURITY_AUDIT_ARCHIVE_RESTORE_REQUESTED", operatorId, manifest.partitionName(),
                "manifestId=" + manifestId);
        return view(requireManifest(manifestId));
    }

    private SecurityAuditArchiveManifestRepository.ArchiveManifest requireManifest(UUID manifestId) {
        if (manifestId == null) {
            throw new DataNotExistException("安全审计归档计划不存在");
        }
        var manifest = manifestRepository.find(manifestId);
        if (manifest == null) {
            throw new DataNotExistException("安全审计归档计划不存在");
        }
        return manifest;
    }

    private static void requirePartition(String partitionName) {
        if (partitionName == null || !partitionName.matches(PARTITION_PATTERN)) {
            throw new IllegalArgumentException("安全审计分区名称不合法");
        }
    }

    private static void requireRange(Instant rangeStart, Instant rangeEnd) {
        if (rangeStart == null || rangeEnd == null || !rangeEnd.isAfter(rangeStart)) {
            throw new IllegalArgumentException("安全审计归档时间范围不合法");
        }
    }

    /** 将 API 不接受的时间文本转换为 UTC Instant。 */
    public static Instant parseInstant(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不能为空");
        }
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(fieldName + " 必须是 ISO-8601 UTC 时间", exception);
        }
    }

    private static ManifestView view(SecurityAuditArchiveManifestRepository.ArchiveManifest manifest) {
        return new ManifestView(manifest.manifestId(), manifest.partitionName(), manifest.rangeStart(), manifest.rangeEnd(),
                manifest.objectUri(), manifest.contentSha256(), manifest.contentLength(), manifest.rowCount(), manifest.state(),
                manifest.archivedAt(), manifest.verifiedAt(), manifest.lastError(), manifest.attempts(), manifest.availableAt());
    }

    /** 对外暴露的非敏感归档状态视图。 */
    public record ManifestView(UUID manifestId,
                               String partitionName,
                               Instant rangeStart,
                               Instant rangeEnd,
                               String objectUri,
                               String contentSha256,
                               Long contentLength,
                               Long rowCount,
                               String state,
                               Instant archivedAt,
                               Instant verifiedAt,
                               String lastError,
                               int attempts,
                               Instant availableAt) {
    }
}

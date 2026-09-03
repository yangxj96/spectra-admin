/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.javabean.vo;

import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;
import com.devops00.spectra.core.upload.javabean.constant.TransportMode;
import com.devops00.spectra.core.upload.javabean.constant.UploadSessionStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/** 上传任务管理响应。 */
@Data
public class FileUploadAdminVO {

    private UUID uploadId;

    private UUID ownerUserId;

    private String originalName;

    private String declaredContentType;

    private Long size;

    private String contentSha256;

    private Long chunkSize;

    private Integer totalParts;

    private Integer completedParts;

    private Long uploadedBytes;

    private StorageProviderType storageProvider;

    private TransportMode transportMode;

    private UploadSessionStatus status;

    private LocalDateTime expiresAt;

    private LocalDateTime lastActivityAt;

    private LocalDateTime completedAt;

    private LocalDateTime verifyStartedAt;

    private LocalDateTime verifyFinishedAt;

    private Long verifyProcessedBytes;

    private Long verifyTotalBytes;

    private String failureCode;

    private Integer cleanupAttempts;

    private LocalDateTime nextCleanupAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

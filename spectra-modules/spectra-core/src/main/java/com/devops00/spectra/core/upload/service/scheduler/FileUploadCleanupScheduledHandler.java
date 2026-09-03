/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.service.scheduler;

import com.devops00.spectra.common.scheduler.ScheduledEffectType;
import com.devops00.spectra.common.scheduler.ScheduledJobContext;
import com.devops00.spectra.common.scheduler.ScheduledJobDescriptor;
import com.devops00.spectra.common.scheduler.ScheduledJobHandler;
import com.devops00.spectra.common.scheduler.ScheduledJobResult;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.core.upload.service.FileUploadCleanupService;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * 文件上传清理的统一调度适配器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
@Component
public class FileUploadCleanupScheduledHandler implements ScheduledJobHandler {

    private static final ScheduledJobDescriptor DESCRIPTOR = ScheduledJobDescriptor.builder()
            .jobKey("file.upload.cleanup")
            .handlerKey("file.upload.cleanup")
            .name("文件上传清理")
            .module("upload")
            .jobType(ScheduledJobType.SYSTEM)
            .runScope(ScheduledRunScope.SINGLETON)
            .scheduleKind(ScheduledScheduleKind.FIXED_DELAY)
            .effectType(ScheduledEffectType.EXTERNAL_IDEMPOTENT)
            .parameterSchema(Map.of())
            .supportedActions(Set.of("VIEW"))
            .executionPolicy(Map.of("timeoutMs", 300000L, "leaseDurationMs", 600000L, "maxAttempts", 1))
            .build();

    private final FileUploadCleanupService cleanupService;

    public FileUploadCleanupScheduledHandler(FileUploadCleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    @Override
    public ScheduledJobDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public ScheduledJobResult execute(ScheduledJobContext context) {
        var summary = cleanupService.cleanupBatch(Instant.now());
        return ScheduledJobResult.builder()
                .status(ScheduledJobResult.Status.SUCCEEDED)
                .resultSummary(summary.resultSummary())
                .build();
    }
}

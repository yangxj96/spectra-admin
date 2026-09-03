/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.service;

import com.devops00.spectra.common.scheduler.ScheduledEffectType;
import com.devops00.spectra.common.scheduler.ScheduledJobContext;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.common.scheduler.ScheduledJobResult;
import com.devops00.spectra.core.upload.service.scheduler.FileUploadCleanupScheduledHandler;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileUploadCleanupScheduledHandlerTest {

    @Test
    void registersAsSingletonFixedDelaySystemTaskWithoutManualTrigger() {
        var handler = new FileUploadCleanupScheduledHandler(mock(FileUploadCleanupService.class));

        var descriptor = handler.descriptor();

        assertEquals("file.upload.cleanup", descriptor.jobKey());
        assertEquals("file.upload.cleanup", descriptor.handlerKey());
        assertEquals("upload", descriptor.module());
        assertEquals(ScheduledJobType.SYSTEM, descriptor.jobType());
        assertEquals(ScheduledRunScope.SINGLETON, descriptor.runScope());
        assertEquals(ScheduledScheduleKind.FIXED_DELAY, descriptor.scheduleKind());
        assertEquals(ScheduledEffectType.EXTERNAL_IDEMPOTENT, descriptor.effectType());
        assertTrue(descriptor.supportedActions().contains("VIEW"));
        assertFalse(descriptor.supportedActions().contains("TRIGGER"));
        assertTrue(descriptor.parameterSchema().isEmpty());
    }

    @Test
    void returnsOnlySanitizedNumericBatchSummary() {
        var cleanupService = mock(FileUploadCleanupService.class);
        var summary = new FileUploadCleanupSummary(2, 1, 3, 4, 5, 6);
        when(cleanupService.cleanupBatch(any())).thenReturn(summary);
        var handler = new FileUploadCleanupScheduledHandler(cleanupService);

        ScheduledJobResult result = handler.execute(ScheduledJobContext.builder()
                .executionId(UUID.randomUUID())
                .jobKey("file.upload.cleanup")
                .handlerKey("file.upload.cleanup")
                .fireKey("fire-key")
                .scheduledAt(Instant.parse("2026-08-31T00:00:00Z"))
                .build());

        assertEquals(ScheduledJobResult.Status.SUCCEEDED, result.status());
        assertEquals(Map.of(
                "expiredSessions", 2L,
                "sessionRetryScheduled", 1L,
                "cleanedSessions", 3L,
                "orphanedAssets", 4L,
                "deletedAssets", 5L,
                "assetRetryScheduled", 6L), result.resultSummary());
        verify(cleanupService).cleanupBatch(any());
    }
}

/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.notification.service;

import com.devops00.spectra.common.scheduler.ScheduledJobContext;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 通知敏感载荷清理适配器契约测试。 */
class NotificationCleanupScheduledHandlerTest {

    @Test
    void delegatesCleanupAndReturnsAnonymousCounts() {
        var cleanupService = mock(NotificationCleanupService.class);
        when(cleanupService.cleanupSensitivePayloads())
                .thenReturn(new NotificationCleanupService.NotificationCleanupResult(2, 5));
        var handler = new NotificationCleanupScheduledHandler(cleanupService);

        var descriptor = handler.descriptor();
        assertEquals("notification.cleanup-sensitive-payload", descriptor.jobKey());
        assertEquals(ScheduledJobType.SYSTEM, descriptor.jobType());
        assertEquals(ScheduledRunScope.SINGLETON, descriptor.runScope());
        var result = handler.execute(context());

        assertEquals(2, result.resultSummary().get("requestCount"));
        assertEquals(5, result.resultSummary().get("taskCount"));
        verify(cleanupService).cleanupSensitivePayloads();
    }

    private static ScheduledJobContext context() {
        return ScheduledJobContext.builder()
                .executionId(UUID.randomUUID())
                .jobKey("notification.cleanup-sensitive-payload")
                .handlerKey("notification.cleanup-sensitive-payload")
                .fireKey("fire-key")
                .scheduledAt(java.time.Instant.now())
                .build();
    }
}

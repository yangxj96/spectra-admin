/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.system.service.scheduler;

import com.devops00.spectra.common.scheduler.ScheduledJobContext;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledLoopContext;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.core.system.service.impl.ServiceMonitorDiagnosticServiceImpl;
import com.devops00.spectra.core.system.service.impl.ServiceMonitorServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/** 系统监控调度适配器契约测试。 */
class SchedulerBusinessHandlerAdapterTest {

    @Test
    void monitorLoopUsesPerInstanceContract() {
        var monitorService = mock(ServiceMonitorServiceImpl.class);
        var handler = new ServiceMonitorLoopHandler(monitorService);

        assertEquals("system.monitor.collect-snapshot", handler.descriptor().jobKey());
        assertEquals(ScheduledJobType.LOOP, handler.descriptor().jobType());
        assertEquals(ScheduledRunScope.PER_INSTANCE, handler.descriptor().runScope());

        var result = handler.runCycle(loopContext(handler.descriptor().jobKey(), handler.descriptor().handlerKey()));

        assertEquals(1, result.processed());
        verify(monitorService).collectSnapshotForScheduler();
    }

    @Test
    void diagnosticCleanupUsesSystemSingletonContract() {
        var diagnosticService = mock(ServiceMonitorDiagnosticServiceImpl.class);
        var handler = new ServiceMonitorDiagnosticCleanupHandler(diagnosticService);

        assertEquals("system.monitor.diagnostic-cleanup", handler.descriptor().jobKey());
        assertEquals(ScheduledJobType.SYSTEM, handler.descriptor().jobType());
        assertEquals(ScheduledRunScope.SINGLETON, handler.descriptor().runScope());

        handler.execute(jobContext(handler.descriptor().jobKey(), handler.descriptor().handlerKey()));

        verify(diagnosticService).cleanupExpiredTasks();
    }

    private static ScheduledLoopContext loopContext(String jobKey, String handlerKey) {
        return ScheduledLoopContext.builder()
                .runtimeId(UUID.randomUUID())
                .jobKey(jobKey)
                .handlerKey(handlerKey)
                .sessionKey("session-a")
                .instanceId("instance-a")
                .startedAt(Instant.now())
                .build();
    }

    private static ScheduledJobContext jobContext(String jobKey, String handlerKey) {
        return ScheduledJobContext.builder()
                .executionId(UUID.randomUUID())
                .jobKey(jobKey)
                .handlerKey(handlerKey)
                .fireKey("fire-key")
                .scheduledAt(Instant.now())
                .build();
    }
}

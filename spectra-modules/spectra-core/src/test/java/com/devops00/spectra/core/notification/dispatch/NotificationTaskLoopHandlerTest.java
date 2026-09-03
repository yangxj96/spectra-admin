/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.notification.dispatch;

import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledLoopContext;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 通知 LOOP 适配器契约测试。 */
class NotificationTaskLoopHandlerTest {

    @Test
    void delegatesWithSchedulerInstanceAndThrottledRecovery() {
        var worker = mock(NotificationTaskWorker.class);
        when(worker.processPending(50, "instance-a", true)).thenReturn(7);
        when(worker.processPending(50, "instance-a", false)).thenReturn(4);
        var handler = new NotificationTaskLoopHandler(worker);

        var descriptor = handler.descriptor();
        assertEquals("notification.task-worker", descriptor.jobKey());
        assertEquals(ScheduledJobType.LOOP, descriptor.jobType());
        assertEquals(ScheduledRunScope.PER_INSTANCE, descriptor.runScope());
        assertEquals(ScheduledScheduleKind.FIXED_DELAY, descriptor.scheduleKind());
        assertTrue(descriptor.supportedActions().contains("DRAIN_STOP"));

        var context = ScheduledLoopContext.builder()
                .runtimeId(UUID.randomUUID())
                .jobKey(descriptor.jobKey())
                .handlerKey(descriptor.handlerKey())
                .sessionKey("session-a")
                .instanceId("instance-a")
                .startedAt(Instant.now())
                .build();
        var result = handler.runCycle(context);
        var secondResult = handler.runCycle(context);

        assertEquals(7, result.processed());
        assertEquals(4, secondResult.processed());
        verify(worker).processPending(eq(50), eq("instance-a"), eq(true));
        verify(worker).processPending(eq(50), eq("instance-a"), eq(false));
    }
}

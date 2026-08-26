/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.oa.contract.service.job;

import com.devops00.spectra.common.scheduler.ScheduledJobContext;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 合同提醒调度适配器契约测试。 */
class ContractReminderScheduledHandlerTest {

    @Test
    void delegatesReminderAndReturnsSentCount() {
        var reminderJob = mock(ContractReminderJob.class);
        when(reminderJob.sendDueMilestoneReminders()).thenReturn(3);
        var handler = new ContractReminderScheduledHandler(reminderJob);

        assertEquals("oa.contract.milestone-reminder", handler.descriptor().jobKey());
        assertEquals(ScheduledJobType.OPS, handler.descriptor().jobType());
        assertEquals(ScheduledRunScope.SINGLETON, handler.descriptor().runScope());

        var result = handler.execute(ScheduledJobContext.builder()
                .executionId(UUID.randomUUID())
                .jobKey(handler.descriptor().jobKey())
                .handlerKey(handler.descriptor().handlerKey())
                .fireKey("fire-key")
                .scheduledAt(Instant.now())
                .build());

        assertEquals(3, result.resultSummary().get("sent"));
        verify(reminderJob).sendDueMilestoneReminders();
    }
}

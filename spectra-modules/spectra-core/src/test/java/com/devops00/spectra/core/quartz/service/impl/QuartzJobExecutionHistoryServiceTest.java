/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.quartz.service.impl;

import com.github.f4b6a3.uuid.UuidCreator;
import com.devops00.spectra.core.quartz.javabean.entity.QuartzJobExecutionHistoryEntity;
import com.devops00.spectra.core.quartz.javabean.enums.QuartzExecutionHistoryStatus;
import com.devops00.spectra.core.quartz.mapper.QuartzJobExecutionHistoryMapper;
import com.devops00.spectra.core.quartz.configuration.QuartzSchedulerProperties;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

class QuartzJobExecutionHistoryServiceTest {

    private final QuartzJobExecutionHistoryMapper mapper = Mockito.mock(QuartzJobExecutionHistoryMapper.class);
    private final QuartzSchedulerProperties properties = Mockito.mock(QuartzSchedulerProperties.class);
    private final JobExecutionContext context = Mockito.mock(JobExecutionContext.class);
    private QuartzJobExecutionHistoryServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        service = new QuartzJobExecutionHistoryServiceImpl(mapper, properties);
        Mockito.doAnswer(invocation -> {
            var entity = invocation.getArgument(0, QuartzJobExecutionHistoryEntity.class);
            if (entity.getId() == null) {
                entity.setId(UuidCreator.getTimeOrderedEpoch());
            }
            return 1;
        }).when(mapper).insert(ArgumentMatchers.any(QuartzJobExecutionHistoryEntity.class));
        JobDetail detail = Mockito.mock(JobDetail.class);
        Mockito.when(detail.getKey()).thenReturn(new JobKey("sample.job", "SPECTRA_BUILTIN"));
        Mockito.doReturn(SampleJob.class).when(detail).getJobClass();
        JobDataMap data = new JobDataMap();
        data.put("spectra.job.type", "sample.job");
        data.put("spectra.parameter.version", "v1");
        data.put("spectra.parameter.sha256", "digest");
        Trigger trigger = mockTrigger();
        Mockito.when(context.getJobDetail()).thenReturn(detail);
        Mockito.when(context.getTrigger()).thenReturn(trigger);
        Mockito.when(context.getMergedJobDataMap()).thenReturn(data);
        Mockito.when(context.getFireInstanceId()).thenReturn("fire-1");
        Mockito.when(context.getScheduledFireTime()).thenReturn(Date.from(Instant.parse("2026-09-09T06:00:00Z")));
        Mockito.when(context.getFireTime()).thenReturn(Date.from(Instant.parse("2026-09-09T06:00:01Z")));
        Mockito.when(context.getJobRunTime()).thenReturn(10L);
        Scheduler scheduler = Mockito.mock(Scheduler.class);
        Mockito.when(context.getScheduler()).thenReturn(scheduler);
        Mockito.when(scheduler.getSchedulerInstanceId()).thenReturn("node-1");
    }

    @Test
    void startedAndCompletedMustUseTheSameFireInstanceHistory() {
        service.started(context);
        service.completed(context, null);
        ArgumentCaptor<QuartzExecutionHistoryStatus> status = ArgumentCaptor.forClass(QuartzExecutionHistoryStatus.class);
        Mockito.verify(mapper)
                .finish(ArgumentMatchers.any(), status.capture(), ArgumentMatchers.any(),
                        ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Assertions.assertThat(status.getValue()).isEqualTo(QuartzExecutionHistoryStatus.SUCCEEDED);
    }

    @Test
    void startedMustLeavePrimaryKeyGenerationToPersistenceHandler() {
        service.started(context);

        ArgumentCaptor<QuartzJobExecutionHistoryEntity> entity = ArgumentCaptor.forClass(
                QuartzJobExecutionHistoryEntity.class);
        Mockito.verify(mapper).insert(entity.capture());
        Assertions.assertThat(entity.getValue().getId()).isNotNull();
        Assertions.assertThat(entity.getValue().getId().version()).isEqualTo(7);
    }

    @Test
    void failedAndVetoedMustUseExplicitTerminalStates() {
        service.started(context);
        service.completed(context, new JobExecutionException(new IllegalStateException("safe")));
        service.vetoed(context);
        ArgumentCaptor<QuartzExecutionHistoryStatus> statuses = ArgumentCaptor.forClass(QuartzExecutionHistoryStatus.class);
        Mockito.verify(mapper)
                .finish(ArgumentMatchers.any(), statuses.capture(), ArgumentMatchers.any(),
                        ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        Assertions.assertThat(statuses.getValue()).isEqualTo(QuartzExecutionHistoryStatus.FAILED);
        ArgumentCaptor<QuartzJobExecutionHistoryEntity> entity = ArgumentCaptor.forClass(QuartzJobExecutionHistoryEntity.class);
        Mockito.verify(mapper, Mockito.times(2)).insert(entity.capture());
        Assertions.assertThat(entity.getAllValues().getLast().getStatus())
                .isEqualTo(QuartzExecutionHistoryStatus.VETOED);
    }

    @Test
    void cleanupMustProcessRunningAndTerminalRowsInConfiguredBatches() {
        Mockito.when(properties.getHistoryRunningTimeout()).thenReturn(Duration.ofHours(1));
        Mockito.when(properties.getHistoryRetentionDays()).thenReturn(90);
        Mockito.when(properties.getHistoryCleanupBatchSize()).thenReturn(2);
        Mockito.when(mapper.abandonExpired(ArgumentMatchers.any(), ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.anyInt()))
                .thenReturn(1);
        Mockito.when(mapper.deleteExpired(ArgumentMatchers.any(), ArgumentMatchers.anyInt())).thenReturn(2, 0);
        Assertions.assertThat(service.cleanup()).isEqualTo(3);
        Mockito.verify(mapper)
                .abandonExpired(ArgumentMatchers.any(), ArgumentMatchers.any(),
                        ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.eq(2));
        Mockito.verify(mapper, Mockito.times(2)).deleteExpired(ArgumentMatchers.any(), ArgumentMatchers.eq(2));
    }

    private Trigger mockTrigger() {
        CronTrigger trigger = Mockito.mock(CronTrigger.class);
        Mockito.when(trigger.getKey()).thenReturn(new TriggerKey("sample.job.trigger", "SPECTRA_BUILTIN"));
        return trigger;
    }

    private static final class SampleJob implements Job {

        @Override
        public void execute(JobExecutionContext context) {
        }
    }
}

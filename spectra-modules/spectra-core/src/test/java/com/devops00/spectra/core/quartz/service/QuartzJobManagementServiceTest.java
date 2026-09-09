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

package com.devops00.spectra.core.quartz.service;

import com.devops00.spectra.common.exception.BuiltinDataException;
import com.devops00.spectra.common.exception.SchedulerDatabaseUnavailableException;
import com.devops00.spectra.common.port.quartz.QuartzJobDefinition;
import com.devops00.spectra.common.port.quartz.QuartzParameterSchema;
import com.devops00.spectra.common.port.quartz.QuartzTriggerTemplate;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.quartz.service.QuartzJobExecutionHistoryService;
import com.devops00.spectra.core.quartz.catalog.QuartzJobCatalog;
import com.devops00.spectra.core.quartz.configuration.QuartzSchedulerLifecycle;
import com.devops00.spectra.core.quartz.javabean.from.QuartzJobCreateFrom;
import com.devops00.spectra.core.quartz.javabean.from.QuartzTriggerFrom;
import com.devops00.spectra.core.quartz.javabean.vo.QuartzJobVO;
import com.devops00.spectra.core.quartz.parameter.QuartzJobParameterValidator;
import com.devops00.spectra.core.quartz.parameter.VersionedJsonJobParameters;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import com.devops00.spectra.core.quartz.service.impl.QuartzJobManagementServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.matchers.GroupMatcher;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class QuartzJobManagementServiceTest {

    private final QuartzSchedulerLifecycle lifecycle = Mockito.mock(QuartzSchedulerLifecycle.class);
    private final QuartzJobCatalog catalog = Mockito.mock(QuartzJobCatalog.class);
    private final QuartzJobParameterValidator validator = Mockito.mock(QuartzJobParameterValidator.class);
    private final QuartzJobExecutionHistoryService historyService = Mockito.mock(QuartzJobExecutionHistoryService.class);
    private final SecurityContextAccessor securityContextAccessor = Mockito.mock(SecurityContextAccessor.class);
    private final TimeMapper timeMapper = new TimeMapper(securityContextAccessor);
    private final Scheduler scheduler = Mockito.mock(Scheduler.class);
    private QuartzJobManagementServiceImpl service;

    @BeforeEach
    void setUp() throws Exception {
        Mockito.when(lifecycle.isReady()).thenReturn(true);
        Mockito.when(lifecycle.scheduler()).thenReturn(scheduler);
        Mockito.when(securityContextAccessor.currentUserZoneId()).thenReturn("Asia/Shanghai");
        QuartzJobDefinition definition = definition(false);
        Mockito.when(catalog.find("sample.job")).thenReturn(Optional.of(definition));
        Mockito.when(catalog.definitions()).thenReturn(List.of(definition));
        Mockito.when(validator.validate(ArgumentMatchers.any(), ArgumentMatchers.any()))
                .thenReturn(new VersionedJsonJobParameters("1", Map.of(), "{\"version\":\"1\"}"));
        Mockito.when(scheduler.getJobDetail(ArgumentMatchers.any())).thenAnswer(invocation -> {
            JobDetailImpl detail = new JobDetailImpl();
            detail.setKey(invocation.getArgument(0));
            detail.setJobClass(SampleJob.class);
            detail.setDescription("Sample Job");
            JobDataMap data = new JobDataMap();
            data.put("spectra.job.type", "sample.job");
            data.put("spectra.parameter.version", "1");
            detail.setJobDataMap(data);
            return detail;
        });
        service = new QuartzJobManagementServiceImpl(lifecycle, catalog, validator, historyService, timeMapper);
    }

    @Test
    void createMustGenerateServerJobKeyAndScheduleExactlyOneTrigger() throws Exception {
        QuartzJobCreateFrom from = new QuartzJobCreateFrom("Sample Job", "sample.job", "{\"version\":\"1\"}",
                new QuartzTriggerFrom(QuartzTriggerTemplate.TriggerType.SIMPLE, null, null, null,
                        1000L, false, QuartzTriggerTemplate.MisfirePolicy.FIRE_ONCE_NOW));
        ArgumentCaptor<JobDetail> detailCaptor = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
        Mockito.when(scheduler.getJobKeys(ArgumentMatchers.any(GroupMatcher.class))).thenReturn(Set.of());
        Mockito.when(scheduler.getTriggersOfJob(ArgumentMatchers.any())).thenReturn(List.of());
        QuartzJobVO result = service.create(from);
        Assertions.assertThat(result.jobKey()).startsWith("SPECTRA_ADMIN.");
        Assertions.assertThat(result.trigger()).isNull();
        Mockito.verify(scheduler).addJob(detailCaptor.capture(), ArgumentMatchers.eq(false));
        Mockito.verify(scheduler).scheduleJob(triggerCaptor.capture());
        Assertions.assertThat(detailCaptor.getValue().getJobDataMap().getString("spectra.job.type"))
                .isEqualTo("sample.job");
        Assertions.assertThat(detailCaptor.getValue().requestsRecovery()).isFalse();
        Assertions.assertThat(triggerCaptor.getValue().getJobKey()).isEqualTo(detailCaptor.getValue().getKey());
    }

    @Test
    void builtInDeleteMustBeRejected() throws Exception {
        JobKey key = new JobKey("sample.job", "SPECTRA_BUILTIN");
        JobDetailImpl detail = new JobDetailImpl();
        detail.setKey(key);
        detail.setJobClass(SampleJob.class);
        JobDataMap data = new JobDataMap();
        data.put("spectra.job.built-in", true);
        detail.setJobDataMap(data);
        Mockito.when(scheduler.getJobKeys(ArgumentMatchers.any(GroupMatcher.class))).thenReturn(Set.of(key));
        Mockito.when(scheduler.getJobDetail(key)).thenReturn(detail);
        Assertions.assertThatThrownBy(() -> service.delete(key.toString()))
                .isInstanceOf(BuiltinDataException.class);
    }

    @Test
    void unavailableSchedulerMustFailClosedAndImmediateTriggerMustNotCreateTrigger() throws Exception {
        Mockito.when(lifecycle.isReady()).thenReturn(false);
        Assertions.assertThatThrownBy(() -> service.triggerNow("sample.job"))
                .isInstanceOf(SchedulerDatabaseUnavailableException.class);
        Mockito.when(lifecycle.isReady()).thenReturn(true);
        JobKey key = new JobKey("sample.job", "SPECTRA_ADMIN");
        Mockito.when(scheduler.getJobKeys(ArgumentMatchers.any(GroupMatcher.class))).thenReturn(Set.of(key));
        service.triggerNow(key.toString());
        Mockito.verify(scheduler).triggerJob(key);
    }

    @Test
    void triggerNextFireTimeMustUseCurrentUserTimeZone() throws Exception {
        JobKey key = new JobKey("sample-job", QuartzJobManagementServiceImpl.ADMIN_GROUP);
        TriggerKey triggerKey = new TriggerKey("sample-job.trigger", QuartzJobManagementServiceImpl.ADMIN_GROUP);
        Instant nextFireInstant = Instant.parse("2026-09-09T00:00:00Z");
        SimpleTrigger trigger = Mockito.mock(SimpleTrigger.class);
        Mockito.when(trigger.getKey()).thenReturn(triggerKey);
        Mockito.when(trigger.getStartTime()).thenReturn(Date.from(nextFireInstant));
        Mockito.when(trigger.getNextFireTime()).thenReturn(Date.from(nextFireInstant));
        Mockito.when(trigger.getRepeatInterval()).thenReturn(1000L);
        Mockito.when(trigger.getRepeatCount()).thenReturn(1);
        Mockito.when(trigger.getMisfireInstruction()).thenReturn(1);
        Mockito.when(scheduler.getJobKeys(ArgumentMatchers.any(GroupMatcher.class))).thenReturn(Set.of(key));
        Mockito.doReturn(List.of(trigger)).when(scheduler).getTriggersOfJob(key);
        Mockito.when(scheduler.getTriggerState(trigger.getKey())).thenReturn(Trigger.TriggerState.NORMAL);

        QuartzJobVO result = service.job(key.toString());

        Assertions.assertThat(result.trigger().nextFireAt())
                .isEqualTo(LocalDateTime.of(2026, 9, 9, 8, 0));
    }

    private QuartzJobDefinition definition(boolean builtIn) {
        return new QuartzJobDefinition() {
            @Override
            public String typeKey() {
                return "sample.job";
            }

            @Override
            public String displayName() {
                return "Sample Job";
            }

            @Override
            public Class<? extends Job> jobClass() {
                return SampleJob.class;
            }

            @Override
            public boolean builtIn() {
                return builtIn;
            }

            @Override
            public Optional<String> builtInJobKey() {
                return builtIn ? Optional.of("sample.job") : Optional.empty();
            }

            @Override
            public QuartzParameterSchema parameterSchema() {
                return QuartzParameterSchema.empty();
            }

            @Override
            public Optional<QuartzTriggerTemplate> defaultTrigger() {
                return Optional.of(QuartzTriggerTemplate.fixedInterval(Duration.ofSeconds(1), null));
            }
        };
    }

    private static final class SampleJob implements Job {

        @Override
        public void execute(JobExecutionContext context) {
        }
    }
}

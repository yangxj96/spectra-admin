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

package com.devops00.spectra.core.scheduler.worker;

import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledLoopCycleResult;
import com.devops00.spectra.common.scheduler.ScheduledLoopHandler;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.core.scheduler.configuration.SchedulerProperties;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerJobEntity;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerLoopRuntimeEntity;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDefinitionStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerRuntimeStatus;
import com.devops00.spectra.core.scheduler.mapper.SchedulerJobMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerLoopRuntimeMapper;
import com.devops00.spectra.core.scheduler.service.LoopErrorAggregator;
import com.devops00.spectra.core.scheduler.service.LoopStateMachine;
import com.devops00.spectra.core.scheduler.service.ScheduledJobRegistry;
import com.devops00.spectra.core.scheduler.service.SchedulerControlCommandService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoopControllerTest {

    @Test
    void normalCycleUpdatesRuntimeWithoutCreatingErrorAggregate() {
        var now = Instant.parse("2026-08-26T00:00:00Z");
        var job = loopJob(ScheduledRunScope.PER_INSTANCE);
        var runtime = runtime(job, now);
        var jobMapper = mock(SchedulerJobMapper.class);
        var runtimeMapper = mock(SchedulerLoopRuntimeMapper.class);
        var registry = mock(ScheduledJobRegistry.class);
        var commandService = mock(SchedulerControlCommandService.class);
        var leaseService = mock(SingletonLoopLeaseService.class);
        var errorAggregator = mock(LoopErrorAggregator.class);
        var handler = mock(ScheduledLoopHandler.class);
        when(commandService.pending(50)).thenReturn(List.of());
        when(jobMapper.selectList(null)).thenReturn(List.of(job));
        when(leaseService.ensureSession(job, "instance-a", now)).thenReturn(Optional.of(runtime));
        when(leaseService.leaseDuration(job)).thenReturn(java.time.Duration.ofSeconds(30));
        when(runtimeMapper.heartbeatRuntime(runtime.getId(), 1L, "instance-a", now,
                now.plusSeconds(30))).thenReturn(1);
        when(runtimeMapper.recordCycle(runtime.getId(), 2L, "instance-a", "RUNNING", now, null,
                0L, 0L, 0L, null, null)).thenReturn(1);
        when(registry.findLoopHandler(job.getJobKey())).thenReturn(Optional.of(handler));
        when(handler.runCycle(any())).thenReturn(ScheduledLoopCycleResult.empty());
        var controller = new LoopController(jobMapper, runtimeMapper, registry, commandService, leaseService,
                errorAggregator, new LoopStateMachine(), new SchedulerProperties(),
                new SchedulerInstanceIdentity("instance-a"), Clock.fixed(now, ZoneOffset.UTC));

        controller.runOnce();

        verify(handler).runCycle(any());
        verify(runtimeMapper).recordCycle(runtime.getId(), 2L, "instance-a", "RUNNING", now, null,
                0L, 0L, 0L, null, null);
        verify(errorAggregator, never()).record(any(), any(), anyString(), anyString(), anyString(), any(), any(), any());
    }

    @Test
    void drainingRuntimeStopsAfterDeadline() {
        var now = Instant.parse("2026-08-26T00:00:00Z");
        var job = loopJob(ScheduledRunScope.PER_INSTANCE);
        job.setDesiredState(com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDesiredState.DRAINING);
        var runtime = runtime(job, now);
        runtime.setStatus(SchedulerRuntimeStatus.DRAINING);
        runtime.setDrainDeadlineAt(now.minusSeconds(1));
        runtime.setVersion(3L);
        var jobMapper = mock(SchedulerJobMapper.class);
        var runtimeMapper = mock(SchedulerLoopRuntimeMapper.class);
        var registry = mock(ScheduledJobRegistry.class);
        var commandService = mock(SchedulerControlCommandService.class);
        var leaseService = mock(SingletonLoopLeaseService.class);
        var errorAggregator = mock(LoopErrorAggregator.class);
        when(commandService.pending(50)).thenReturn(List.of());
        when(jobMapper.selectList(null)).thenReturn(List.of(job));
        when(runtimeMapper.selectByJobId(job.getId())).thenReturn(List.of(runtime));
        when(runtimeMapper.transitionRuntime(runtime.getId(), 3L, "instance-a", "STOPPED", "排空截止时间到达",
                null, now)).thenReturn(1);
        var controller = new LoopController(jobMapper, runtimeMapper, registry, commandService, leaseService,
                errorAggregator, new LoopStateMachine(), new SchedulerProperties(),
                new SchedulerInstanceIdentity("instance-a"), Clock.fixed(now, ZoneOffset.UTC));

        controller.runOnce();

        verify(runtimeMapper).transitionRuntime(runtime.getId(), 3L, "instance-a", "STOPPED", "排空截止时间到达",
                null, now);
    }

    private static SchedulerJobEntity loopJob(ScheduledRunScope scope) {
        var job = new SchedulerJobEntity();
        job.setId(UUID.randomUUID());
        job.setJobKey("test.loop");
        job.setHandlerKey("test.loop");
        job.setJobType(ScheduledJobType.LOOP);
        job.setRunScope(scope);
        job.setDefinitionStatus(SchedulerDefinitionStatus.REGISTERED);
        job.setDesiredState(com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDesiredState.RUNNING);
        job.setScheduleKind(ScheduledScheduleKind.FIXED_DELAY);
        job.setFixedDelayMs(1000L);
        job.setInitialDelayMs(0L);
        job.setRevision(1L);
        job.setParameters(Map.of());
        job.setExecutionPolicy(Map.of("leaseDurationMs", 30000L));
        job.setVersion(0L);
        return job;
    }

    private static SchedulerLoopRuntimeEntity runtime(SchedulerJobEntity job, Instant now) {
        var runtime = new SchedulerLoopRuntimeEntity();
        runtime.setId(UUID.randomUUID());
        runtime.setJobId(job.getId());
        runtime.setSessionKey("test-session");
        runtime.setInstanceId("instance-a");
        runtime.setStatus(SchedulerRuntimeStatus.RUNNING);
        runtime.setStartedAt(now);
        runtime.setLeaseExpiresAt(now.plusSeconds(30));
        runtime.setVersion(1L);
        runtime.setTotalCycles(0L);
        runtime.setTotalProcessed(0L);
        runtime.setTotalFailed(0L);
        runtime.setConsecutiveErrorCount(0L);
        return runtime;
    }
}

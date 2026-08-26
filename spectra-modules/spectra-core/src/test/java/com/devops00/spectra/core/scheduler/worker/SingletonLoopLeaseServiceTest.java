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
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerJobEntity;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerLoopRuntimeEntity;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDefinitionStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDesiredState;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerRuntimeStatus;
import com.devops00.spectra.core.scheduler.mapper.SchedulerJobMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerLoopRuntimeMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SingletonLoopLeaseServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-26T00:00:00Z");

    @Test
    void perInstanceDoesNotCreateDuplicateSessionForSameInstance() {
        var job = job(ScheduledRunScope.PER_INSTANCE);
        var existing = runtime(job, "instance-a", NOW.plusSeconds(20), 1L);
        var jobMapper = mock(SchedulerJobMapper.class);
        var runtimeMapper = mock(SchedulerLoopRuntimeMapper.class);
        when(jobMapper.selectByIdForUpdate(job.getId())).thenReturn(job);
        when(runtimeMapper.selectByJobId(job.getId())).thenReturn(List.of(), List.of(existing));
        when(runtimeMapper.insert(any(SchedulerLoopRuntimeEntity.class))).thenReturn(1);
        when(runtimeMapper.claimStartingRuntime(any(), anyLong(), any(), any(), any())).thenReturn(1);
        var service = new SingletonLoopLeaseService(jobMapper, runtimeMapper);

        var first = service.ensureSession(job, "instance-a", NOW);
        var second = service.ensureSession(job, "instance-a", NOW.plusSeconds(1));

        assertTrue(first.isPresent());
        assertEquals(existing, second.orElseThrow());
        verify(runtimeMapper).insert(any(SchedulerLoopRuntimeEntity.class));
    }

    @Test
    void singletonRefusesSecondLiveLease() {
        var job = job(ScheduledRunScope.SINGLETON);
        var existing = runtime(job, "instance-a", NOW.plusSeconds(20), 1L);
        var jobMapper = mock(SchedulerJobMapper.class);
        var runtimeMapper = mock(SchedulerLoopRuntimeMapper.class);
        when(jobMapper.selectByIdForUpdate(job.getId())).thenReturn(job);
        when(runtimeMapper.selectByJobId(job.getId())).thenReturn(List.of(existing));
        var service = new SingletonLoopLeaseService(jobMapper, runtimeMapper);

        var result = service.ensureSession(job, "instance-b", NOW);

        assertTrue(result.isEmpty());
        verify(runtimeMapper, never()).insert(any(SchedulerLoopRuntimeEntity.class));
    }

    @Test
    void expiredLeaseIsReclaimedByVersionBeforeNewSession() {
        var job = job(ScheduledRunScope.SINGLETON);
        var expired = runtime(job, "instance-a", NOW.minusSeconds(1), 4L);
        var jobMapper = mock(SchedulerJobMapper.class);
        var runtimeMapper = mock(SchedulerLoopRuntimeMapper.class);
        when(jobMapper.selectByIdForUpdate(job.getId())).thenReturn(job);
        when(runtimeMapper.selectByJobId(job.getId())).thenReturn(List.of(expired));
        when(runtimeMapper.reclaimExpiredRuntime(expired.getId(), 4L, "CRASHED", "循环租约已过期", NOW))
                .thenReturn(1);
        when(runtimeMapper.insert(any(SchedulerLoopRuntimeEntity.class))).thenReturn(1);
        when(runtimeMapper.claimStartingRuntime(any(), anyLong(), any(), any(), any())).thenReturn(1);
        var service = new SingletonLoopLeaseService(jobMapper, runtimeMapper);

        assertTrue(service.ensureSession(job, "instance-b", NOW).isPresent());

        verify(runtimeMapper).reclaimExpiredRuntime(expired.getId(), 4L, "CRASHED", "循环租约已过期", NOW);
    }

    @Test
    void gracefulShutdownStopsOwnedActiveSessions() {
        var job = job(ScheduledRunScope.PER_INSTANCE);
        var existing = runtime(job, "instance-a", NOW.plusSeconds(20), 1L);
        var otherInstance = runtime(job, "instance-b", NOW.plusSeconds(20), 2L);
        var stopped = runtime(job, "instance-a", NOW.plusSeconds(20), 3L);
        stopped.setStatus(SchedulerRuntimeStatus.STOPPED);
        var jobMapper = mock(SchedulerJobMapper.class);
        var runtimeMapper = mock(SchedulerLoopRuntimeMapper.class);
        when(jobMapper.selectList(null)).thenReturn(List.of(job));
        when(runtimeMapper.selectByJobId(job.getId())).thenReturn(List.of(existing, otherInstance, stopped));
        when(runtimeMapper.transitionRuntime(existing.getId(), 1L, "instance-a", "STOPPED", "应用实例正常停止",
                null, NOW)).thenReturn(1);
        var service = new SingletonLoopLeaseService(jobMapper, runtimeMapper);

        service.stopOwnedSessions("instance-a", NOW);

        verify(runtimeMapper).transitionRuntime(existing.getId(), 1L, "instance-a", "STOPPED", "应用实例正常停止",
                null, NOW);
        verify(runtimeMapper, never()).transitionRuntime(otherInstance.getId(), 2L, "instance-a", "STOPPED",
                "应用实例正常停止", null, NOW);
        verify(runtimeMapper, never()).transitionRuntime(stopped.getId(), 3L, "instance-a", "STOPPED",
                "应用实例正常停止", null, NOW);
    }

    private static SchedulerJobEntity job(ScheduledRunScope scope) {
        var job = new SchedulerJobEntity();
        job.setId(UUID.randomUUID());
        job.setJobKey("test.loop");
        job.setJobType(ScheduledJobType.LOOP);
        job.setRunScope(scope);
        job.setDefinitionStatus(SchedulerDefinitionStatus.REGISTERED);
        job.setDesiredState(SchedulerDesiredState.RUNNING);
        job.setExecutionPolicy(Map.of("leaseDurationMs", 30000L));
        return job;
    }

    private static SchedulerLoopRuntimeEntity runtime(SchedulerJobEntity job, String instanceId,
                                                      Instant expiresAt, long version) {
        var runtime = new SchedulerLoopRuntimeEntity();
        runtime.setId(UUID.randomUUID());
        runtime.setJobId(job.getId());
        runtime.setSessionKey("session-" + instanceId);
        runtime.setInstanceId(instanceId);
        runtime.setStatus(SchedulerRuntimeStatus.RUNNING);
        runtime.setStartedAt(NOW.minusSeconds(30));
        runtime.setLeaseExpiresAt(expiresAt);
        runtime.setVersion(version);
        return runtime;
    }
}

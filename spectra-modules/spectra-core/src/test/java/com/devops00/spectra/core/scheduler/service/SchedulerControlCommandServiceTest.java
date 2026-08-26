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

package com.devops00.spectra.core.scheduler.service;

import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerControlCommandEntity;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerJobEntity;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerLoopRuntimeEntity;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerCommandType;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerRuntimeStatus;
import com.devops00.spectra.core.scheduler.mapper.SchedulerControlCommandMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerJobMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerLoopRuntimeMapper;
import com.devops00.spectra.core.scheduler.worker.SingletonLoopLeaseService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SchedulerControlCommandServiceTest {

    @Test
    void duplicateIdempotencyKeyReturnsOriginalCommand() {
        var mapper = mock(SchedulerControlCommandMapper.class);
        var original = new SchedulerControlCommandEntity();
        original.setId(UUID.randomUUID());
        when(mapper.selectByIdempotencyKey("same-request")).thenReturn(original);
        var service = new SchedulerControlCommandService(mapper);

        var result = service.request(UUID.randomUUID(), SchedulerCommandType.START, null, null, null,
                "same-request", "operator reason", UUID.randomUUID(), Instant.EPOCH, null);

        assertSame(original, result);
        verify(mapper, never()).insertIfAbsent(org.mockito.ArgumentMatchers.any(SchedulerControlCommandEntity.class));
    }

    @Test
    void staleTargetSessionIsRejectedBeforeStateMutation() {
        var commandMapper = mock(SchedulerControlCommandMapper.class);
        var jobMapper = mock(SchedulerJobMapper.class);
        var runtimeMapper = mock(SchedulerLoopRuntimeMapper.class);
        var leaseService = mock(SingletonLoopLeaseService.class);
        var jobId = UUID.randomUUID();
        var runtimeId = UUID.randomUUID();
        var job = new SchedulerJobEntity();
        job.setId(jobId);
        job.setJobType(ScheduledJobType.LOOP);
        job.setVersion(3L);
        var runtime = new SchedulerLoopRuntimeEntity();
        runtime.setId(runtimeId);
        runtime.setJobId(jobId);
        runtime.setSessionKey("session-current");
        runtime.setVersion(7L);
        runtime.setStatus(SchedulerRuntimeStatus.RUNNING);
        runtime.setInstanceId("instance-a");
        var command = command(SchedulerCommandType.FORCE_STOP, jobId, runtimeId, "session-old", 6L);
        when(jobMapper.selectByIdForUpdate(jobId)).thenReturn(job);
        when(runtimeMapper.selectById(runtimeId)).thenReturn(runtime);
        var service = new SchedulerControlCommandService(commandMapper, jobMapper, runtimeMapper,
                leaseService, new LoopStateMachine(), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

        assertThrows(IllegalStateException.class, () -> service.apply(command, Instant.EPOCH, "instance-a"));
        verify(runtimeMapper, never()).transitionRuntime(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void controlCommandUsesCurrentVersionWhenTargetSessionIsStillTheSame() {
        var commandMapper = mock(SchedulerControlCommandMapper.class);
        var jobMapper = mock(SchedulerJobMapper.class);
        var runtimeMapper = mock(SchedulerLoopRuntimeMapper.class);
        var leaseService = mock(SingletonLoopLeaseService.class);
        var jobId = UUID.randomUUID();
        var runtimeId = UUID.randomUUID();
        var job = new SchedulerJobEntity();
        job.setId(jobId);
        job.setJobType(ScheduledJobType.LOOP);
        job.setVersion(3L);
        var runtime = new SchedulerLoopRuntimeEntity();
        runtime.setId(runtimeId);
        runtime.setJobId(jobId);
        runtime.setSessionKey("session-current");
        runtime.setVersion(8L);
        runtime.setStatus(SchedulerRuntimeStatus.RUNNING);
        runtime.setInstanceId("instance-a");
        var command = command(SchedulerCommandType.FORCE_STOP, jobId, runtimeId, "session-current", 7L);
        when(jobMapper.selectByIdForUpdate(jobId)).thenReturn(job);
        when(runtimeMapper.selectById(runtimeId)).thenReturn(runtime);
        when(runtimeMapper.transitionRuntime(runtimeId, 8L, "instance-a", "STOPPED", "operator reason",
                null, Instant.EPOCH)).thenReturn(1);
        when(jobMapper.updateDesiredState(jobId, 3L, "STOPPED")).thenReturn(1);
        var service = new SchedulerControlCommandService(commandMapper, jobMapper, runtimeMapper,
                leaseService, new LoopStateMachine(), Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

        var result = service.apply(command, Instant.EPOCH, "instance-a");

        assertEquals("STOPPED", result.resultCode());
        verify(runtimeMapper).transitionRuntime(runtimeId, 8L, "instance-a", "STOPPED", "operator reason",
                null, Instant.EPOCH);
    }

    private static SchedulerControlCommandEntity command(SchedulerCommandType type, UUID jobId, UUID runtimeId,
                                                         String sessionKey, long expectedVersion) {
        var command = new SchedulerControlCommandEntity();
        command.setId(UUID.randomUUID());
        command.setJobId(jobId);
        command.setTargetRuntimeId(runtimeId);
        command.setTargetSessionKey(sessionKey);
        command.setExpectedRuntimeVersion(expectedVersion);
        command.setCommandType(type);
        command.setReason("operator reason");
        command.setVersion(1L);
        return command;
    }
}

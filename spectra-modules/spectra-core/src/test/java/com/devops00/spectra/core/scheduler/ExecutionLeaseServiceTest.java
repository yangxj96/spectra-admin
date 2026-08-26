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

package com.devops00.spectra.core.scheduler;

import com.devops00.spectra.core.scheduler.mapper.SchedulerExecutionMapper;
import com.devops00.spectra.core.scheduler.service.ExecutionLeaseService;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExecutionLeaseServiceTest {

    @Test
    void onlySuccessfulClaimAndCompletionCasAreReportedAsSuccess() {
        var mapper = mock(SchedulerExecutionMapper.class);
        var executionId = UUID.randomUUID();
        var lockedAt = Instant.parse("2026-08-26T00:00:00Z");
        var expiresAt = lockedAt.plusSeconds(30);
        var lease = new ExecutionLeaseService.SchedulerExecutionLease(
                executionId, 4L, "instance-a", lockedAt, expiresAt);
        when(mapper.claimExecution(executionId, 4L, "instance-a", lockedAt, expiresAt)).thenReturn(1);
        when(mapper.completeExecution(executionId, 5L, "instance-a", "SUCCEEDED", expiresAt))
                .thenReturn(0);
        var service = new ExecutionLeaseService(mapper);

        assertTrue(service.claim(lease));
        assertFalse(service.complete(executionId, 5L, "instance-a", "SUCCEEDED", expiresAt));
        verify(mapper).claimExecution(executionId, 4L, "instance-a", lockedAt, expiresAt);
        verify(mapper).completeExecution(eq(executionId), eq(5L), eq("instance-a"), eq("SUCCEEDED"), any());
    }
}

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

import com.devops00.spectra.core.scheduler.configuration.SchedulerProperties;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerJobEntity;
import com.devops00.spectra.core.scheduler.mapper.SchedulerJobMapper;
import com.devops00.spectra.core.scheduler.service.ScheduledJobRegistry;
import com.devops00.spectra.core.scheduler.service.SchedulerBootstrapService;
import com.devops00.spectra.core.scheduler.service.SchedulerTimeZoneResolver;
import com.devops00.spectra.core.scheduler.worker.SchedulerInstanceIdentity;
import com.devops00.spectra.core.scheduler.worker.SingletonLoopLeaseService;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SchedulerBootstrapServiceTest {

    @Test
    void databaseFailureLeavesSchedulerNotReadyAndDoesNotStartDispatch() {
        var jobMapper = mock(SchedulerJobMapper.class);
        when(jobMapper.selectList(any()))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"))
                .thenReturn(List.of());
        var executor = Executors.newSingleThreadScheduledExecutor();
        try {
            var service = service(jobMapper, executor);

            service.start();

            assertFalse(service.isReady());
            assertFalse(service.isRunning());
            assertTrue("DATABASE_UNAVAILABLE".equals(service.getFailureCode()));
            verify(jobMapper, never()).advanceNextFire(any(), any(Long.class), any());

            service.retryStartIfNeeded();

            assertTrue(service.isReady());
            assertTrue(service.isRunning());
            service.stop();
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void missingHandlerMarksJobUnavailableWithoutDeletingHistory() {
        var jobMapper = mock(SchedulerJobMapper.class);
        var job = new SchedulerJobEntity();
        job.setJobKey("missing.handler");
        job.setVersion(0L);
        job.setDefinitionStatus(com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDefinitionStatus.REGISTERED);
        job.setDesiredState(com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDesiredState.DISABLED);
        when(jobMapper.selectList(any())).thenReturn(List.of(job));
        when(jobMapper.updateDefinitionState(any(), any(Long.class), any(), any())).thenReturn(1);
        var executor = Executors.newSingleThreadScheduledExecutor();
        try {
            var service = service(jobMapper, executor);

            service.start();

            assertTrue(service.isReady());
            assertTrue(service.isRunning());
            assertTrue(job.getDefinitionStatus() == com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDefinitionStatus.UNAVAILABLE);
            verify(jobMapper).updateDefinitionState(
                    any(), any(Long.class), any(), any());
        } finally {
            serviceStop(executor);
        }
    }

    @Test
    void gracefulStopReleasesLoopSessionsOwnedByThisInstance() {
        var jobMapper = mock(SchedulerJobMapper.class);
        when(jobMapper.selectList(any())).thenReturn(List.of());
        var loopLeaseService = mock(SingletonLoopLeaseService.class);
        var executor = Executors.newSingleThreadScheduledExecutor();
        try {
            var service = service(jobMapper, executor, loopLeaseService);

            service.start();
            service.stop();

            verify(loopLeaseService).stopOwnedSessions(
                    "instance-a", Instant.parse("2026-08-26T00:00:00Z"));
        } finally {
            serviceStop(executor);
        }
    }

    private static SchedulerBootstrapService service(SchedulerJobMapper jobMapper,
                                                     java.util.concurrent.ScheduledExecutorService executor) {
        return service(jobMapper, executor, mock(SingletonLoopLeaseService.class));
    }

    private static SchedulerBootstrapService service(SchedulerJobMapper jobMapper,
                                                     java.util.concurrent.ScheduledExecutorService executor,
                                                     SingletonLoopLeaseService loopLeaseService) {
        var registry = mock(ScheduledJobRegistry.class);
        when(registry.find("missing.handler")).thenReturn(Optional.empty());
        var resolver = mock(SchedulerTimeZoneResolver.class);
        when(resolver.resolve()).thenReturn(ZoneOffset.UTC);
        var properties = new SchedulerProperties();
        properties.setPollInterval(java.time.Duration.ofHours(1));
        return new SchedulerBootstrapService(
                jobMapper,
                registry,
                resolver,
                executor,
                properties,
                Clock.fixed(Instant.parse("2026-08-26T00:00:00Z"), ZoneOffset.UTC),
                Optional.empty(),
                loopLeaseService,
                new SchedulerInstanceIdentity("instance-a"));
    }

    private static void serviceStop(java.util.concurrent.ScheduledExecutorService executor) {
        executor.shutdownNow();
    }
}

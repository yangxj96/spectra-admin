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

package com.devops00.spectra.core.quartz.configuration;

import com.devops00.spectra.core.quartz.catalog.QuartzJobRegistrar;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.mockito.Mockito;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Quartz 生命周期状态、恢复重试和优雅停止契约。 */
class QuartzSchedulerLifecycleTest {

    @Test
    void jdbcInitializationFailureMustLeaveApplicationLifecycleAlive() throws SchedulerException {
        var scheduler = Mockito.mock(Scheduler.class);
        var properties = new QuartzSchedulerProperties();
        var registrar = Mockito.mock(QuartzJobRegistrar.class);
        var retryExecutor = Mockito.mock(ScheduledExecutorService.class);
        var retryFuture = Mockito.mock(ScheduledFuture.class);
        when(retryExecutor.scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any()))
                .thenReturn(retryFuture);
        doThrow(new SchedulerException("database unavailable")).when(scheduler).start();

        var lifecycle = new QuartzSchedulerLifecycle(scheduler, properties, registrar, retryExecutor);

        lifecycle.start();

        assertThat(lifecycle.getState()).isEqualTo(QuartzSchedulerLifecycle.State.NOT_READY);
        assertThat(lifecycle.isReady()).isFalse();
        assertThat(lifecycle.getFailureCode()).isEqualTo("DATABASE_UNAVAILABLE");
        verify(retryExecutor).scheduleWithFixedDelay(any(Runnable.class), anyLong(), anyLong(), any());
        verify(registrar, never()).register(scheduler);
    }

    @Test
    void successfulStartMustRegisterBuiltInsAndGracefulStopMustWait() throws SchedulerException {
        var scheduler = Mockito.mock(Scheduler.class);
        when(scheduler.isShutdown()).thenReturn(false);
        var registrar = Mockito.mock(QuartzJobRegistrar.class);
        var retryExecutor = Mockito.mock(ScheduledExecutorService.class);
        var lifecycle = new QuartzSchedulerLifecycle(scheduler, new QuartzSchedulerProperties(), registrar, retryExecutor);

        lifecycle.start();
        lifecycle.stop();

        assertThat(lifecycle.getState()).isEqualTo(QuartzSchedulerLifecycle.State.STOPPED);
        verify(registrar).register(scheduler);
        verify(scheduler).shutdown(true);
    }

    @Test
    void disabledSchedulerMustNotStartQuartz() throws SchedulerException {
        var scheduler = Mockito.mock(Scheduler.class);
        var properties = new QuartzSchedulerProperties();
        properties.setEnabled(false);
        var lifecycle = new QuartzSchedulerLifecycle(scheduler, properties,
                Mockito.mock(QuartzJobRegistrar.class), Mockito.mock(ScheduledExecutorService.class));

        lifecycle.start();

        assertThat(lifecycle.getState()).isEqualTo(QuartzSchedulerLifecycle.State.NOT_READY);
        assertThat(lifecycle.getFailureCode()).isEqualTo("DISABLED");
        verify(scheduler, never()).start();
    }
}

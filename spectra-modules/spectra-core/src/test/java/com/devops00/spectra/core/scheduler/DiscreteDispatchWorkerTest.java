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
import com.devops00.spectra.core.scheduler.service.SchedulerExecutionService;
import com.devops00.spectra.core.scheduler.worker.DiscreteDispatchWorker;
import com.devops00.spectra.core.scheduler.worker.SchedulerInstanceIdentity;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

class DiscreteDispatchWorkerTest {

    @Test
    void tickUsesConfiguredBatchAndStableInstanceIdentity() {
        var executionService = mock(SchedulerExecutionService.class);
        var properties = new SchedulerProperties();
        properties.setDueBatchSize(7);
        var worker = new DiscreteDispatchWorker(
                executionService, properties, new SchedulerInstanceIdentity("instance-a"), Runnable::run);

        worker.runOnce();

        verify(executionService).dispatchDueJobs(any(), eq(7));
        verify(executionService).executeClaimable(any(), eq(7), eq("instance-a"));
    }

    @Test
    void doesNotStartAnotherExecutionDrainWhileThePreviousDrainIsInFlight() {
        var executionService = mock(SchedulerExecutionService.class);
        var properties = new SchedulerProperties();
        var executor = new HoldingExecutor();
        var worker = new DiscreteDispatchWorker(
                executionService, properties, new SchedulerInstanceIdentity("instance-a"), executor);

        worker.runOnce();
        worker.runOnce();

        verify(executionService, never()).executeClaimable(any(), any(Integer.class), any());
        executor.run();
        verify(executionService).executeClaimable(any(), eq(properties.getDueBatchSize()), eq("instance-a"));
    }

    private static final class HoldingExecutor implements Executor {

        private Runnable command;

        @Override
        public void execute(Runnable command) {
            if (this.command != null) {
                throw new IllegalStateException("test executor already has a command");
            }
            this.command = command;
        }

        private void run() {
            Runnable current = command;
            command = null;
            current.run();
        }
    }
}

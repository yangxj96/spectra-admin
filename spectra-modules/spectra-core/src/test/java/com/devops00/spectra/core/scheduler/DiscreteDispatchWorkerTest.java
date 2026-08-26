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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DiscreteDispatchWorkerTest {

    @Test
    void tickUsesConfiguredBatchAndStableInstanceIdentity() {
        var executionService = mock(SchedulerExecutionService.class);
        var properties = new SchedulerProperties();
        properties.setDueBatchSize(7);
        var worker = new DiscreteDispatchWorker(
                executionService, properties, new SchedulerInstanceIdentity("instance-a"));

        worker.runOnce();

        verify(executionService).dispatchDueJobs(any(), eq(7));
        verify(executionService).executeClaimable(any(), eq(7), eq("instance-a"));
    }
}

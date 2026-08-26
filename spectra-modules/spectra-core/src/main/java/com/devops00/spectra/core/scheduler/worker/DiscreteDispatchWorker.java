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

import com.devops00.spectra.core.scheduler.configuration.SchedulerProperties;
import com.devops00.spectra.core.scheduler.service.SchedulerExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;

/** 单次调度 tick 的离散任务派发器。 */
@Component
public class DiscreteDispatchWorker {

    private final SchedulerExecutionService executionService;
    private final SchedulerProperties properties;
    private final SchedulerInstanceIdentity instanceIdentity;
    private final Clock clock;

    @Autowired
    public DiscreteDispatchWorker(SchedulerExecutionService executionService,
                                  SchedulerProperties properties,
                                  SchedulerInstanceIdentity instanceIdentity) {
        this(executionService, properties, instanceIdentity, Clock.systemUTC());
    }

    DiscreteDispatchWorker(SchedulerExecutionService executionService,
                           SchedulerProperties properties,
                           SchedulerInstanceIdentity instanceIdentity,
                           Clock clock) {
        this.executionService = executionService;
        this.properties = properties;
        this.instanceIdentity = instanceIdentity;
        this.clock = clock;
    }

    /** 派发到期执行并处理本实例可领取的队列记录。 */
    public void runOnce() {
        var now = clock.instant();
        executionService.dispatchDueJobs(now, properties.getDueBatchSize());
        executionService.executeClaimable(now, properties.getDueBatchSize(), instanceIdentity.value());
    }
}

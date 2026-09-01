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
import com.devops00.spectra.core.scheduler.service.SchedulerDatabaseUnavailableEvent;
import com.devops00.spectra.core.scheduler.service.SchedulerExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/** 单次调度 tick 的离散任务派发器。 */
@Slf4j
@Component
public class DiscreteDispatchWorker {

    private final SchedulerExecutionService executionService;
    private final SchedulerProperties properties;
    private final SchedulerInstanceIdentity instanceIdentity;
    private final Clock clock;
    private final Executor executionExecutor;
    private final ApplicationEventPublisher eventPublisher;
    private final AtomicBoolean executionDrainInFlight = new AtomicBoolean();

    public DiscreteDispatchWorker(SchedulerExecutionService executionService,
                                  SchedulerProperties properties,
                                  SchedulerInstanceIdentity instanceIdentity) {
        this(executionService, properties, instanceIdentity, Runnable::run, event -> {
        }, Clock.systemUTC());
    }

    DiscreteDispatchWorker(SchedulerExecutionService executionService,
                           SchedulerProperties properties,
                           SchedulerInstanceIdentity instanceIdentity,
                           Clock clock) {
        this(executionService, properties, instanceIdentity, Runnable::run, event -> {
        }, clock);
    }

    public DiscreteDispatchWorker(SchedulerExecutionService executionService,
                                  SchedulerProperties properties,
                                  SchedulerInstanceIdentity instanceIdentity,
                                  Executor executionExecutor) {
        this(executionService, properties, instanceIdentity, executionExecutor, event -> {
        }, Clock.systemUTC());
    }

    @Autowired
    public DiscreteDispatchWorker(SchedulerExecutionService executionService,
                                  SchedulerProperties properties,
                                  SchedulerInstanceIdentity instanceIdentity,
                                  @Qualifier("schedulerExecutionExecutor") Executor executionExecutor,
                                  ApplicationEventPublisher eventPublisher) {
        this(executionService, properties, instanceIdentity, executionExecutor, eventPublisher, Clock.systemUTC());
    }

    DiscreteDispatchWorker(SchedulerExecutionService executionService,
                           SchedulerProperties properties,
                           SchedulerInstanceIdentity instanceIdentity,
                           Executor executionExecutor,
                           ApplicationEventPublisher eventPublisher,
                           Clock clock) {
        this.executionService = executionService;
        this.properties = properties;
        this.instanceIdentity = instanceIdentity;
        this.clock = clock;
        this.executionExecutor = executionExecutor;
        this.eventPublisher = eventPublisher;
    }

    /** 派发到期执行并处理本实例可领取的队列记录。 */
    public void runOnce() {
        var now = clock.instant();
        executionService.dispatchDueJobs(now, properties.getDueBatchSize());
        if (!executionDrainInFlight.compareAndSet(false, true)) {
            return;
        }
        try {
            executionExecutor.execute(() -> {
                try {
                    executionService.executeClaimable(clock.instant(), properties.getDueBatchSize(), instanceIdentity.value());
                } catch (DataAccessException exception) {
                    eventPublisher.publishEvent(new SchedulerDatabaseUnavailableEvent(exception));
                } catch (RuntimeException exception) {
                    // 处理器异常由 SchedulerExecutionService 转换为 UNKNOWN；这里不伪造成功结果。
                    log.warn("调度执行批次失败，等待执行服务回写状态", exception);
                } finally {
                    executionDrainInFlight.set(false);
                }
            });
        } catch (RuntimeException exception) {
            executionDrainInFlight.set(false);
            throw exception;
        }
    }
}

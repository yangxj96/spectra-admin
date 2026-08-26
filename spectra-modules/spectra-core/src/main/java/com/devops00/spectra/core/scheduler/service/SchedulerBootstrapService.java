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
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.core.scheduler.configuration.SchedulerProperties;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerJobEntity;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDefinitionStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDesiredState;
import com.devops00.spectra.core.scheduler.mapper.SchedulerJobMapper;
import com.devops00.spectra.core.scheduler.worker.SchedulerInstanceIdentity;
import com.devops00.spectra.core.scheduler.worker.SingletonLoopLeaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.support.CronExpression;

/** 调度启动门禁和内部固定周期 tick 生命周期。 */
@Slf4j
@Component
public class SchedulerBootstrapService implements SmartLifecycle {

    private static final int LIFECYCLE_PHASE = Integer.MAX_VALUE - 100;

    private final SchedulerJobMapper jobMapper;
    private final ScheduledJobRegistry registry;
    private final SchedulerTimeZoneResolver timeZoneResolver;
    private final ScheduledExecutorService tickExecutor;
    private final SchedulerProperties properties;
    private final Clock clock;
    private final Optional<SchedulerTick> schedulerTick;
    private final SingletonLoopLeaseService loopLeaseService;
    private final SchedulerInstanceIdentity instanceIdentity;

    private volatile boolean running;
    private volatile boolean ready;
    private volatile String state = "NOT_READY";
    private volatile String failureCode = "NOT_STARTED";
    private volatile ZoneId systemZone = ZoneId.of("UTC");
    private volatile ScheduledFuture<?> tickFuture;

    public SchedulerBootstrapService(SchedulerJobMapper jobMapper,
                                     ScheduledJobRegistry registry,
                                     SchedulerTimeZoneResolver timeZoneResolver,
                                     ScheduledExecutorService tickExecutor,
                                     SchedulerProperties properties,
                                     Clock clock,
                                     Optional<SchedulerTick> schedulerTick,
                                     SingletonLoopLeaseService loopLeaseService,
                                     SchedulerInstanceIdentity instanceIdentity) {
        this.jobMapper = jobMapper;
        this.registry = registry;
        this.timeZoneResolver = timeZoneResolver;
        this.tickExecutor = tickExecutor;
        this.properties = properties;
        this.clock = clock;
        this.schedulerTick = schedulerTick == null ? Optional.empty() : schedulerTick;
        this.loopLeaseService = loopLeaseService;
        this.instanceIdentity = instanceIdentity;
    }

    @Override
    public synchronized void start() {
        if (running) {
            return;
        }
        if (!properties.isEnabled()) {
            setNotReady("DISABLED");
            state = "DISABLED";
            return;
        }
        try {
            validateProperties();
            var jobs = jobMapper.selectList(null);
            systemZone = timeZoneResolver.resolve();
            synchronizeDefinitions(jobs);
            initializeNextFire(jobs, systemZone);
            tickFuture = tickExecutor.scheduleWithFixedDelay(
                    this::runTick, 0, properties.getPollInterval().toMillis(), TimeUnit.MILLISECONDS);
            ready = true;
            running = true;
            state = "READY";
            failureCode = null;
        } catch (DataAccessException exception) {
            setNotReady("DATABASE_UNAVAILABLE");
            log.error("调度器数据库不可用，保持未就绪并停止派发", exception);
        } catch (RuntimeException exception) {
            setNotReady("BOOTSTRAP_FAILED");
            log.error("调度器启动门禁失败，保持未就绪并停止派发", exception);
        }
    }

    @Override
    public synchronized void stop() {
        var wasActive = running || ready;
        cancelTick();
        running = false;
        ready = false;
        state = "STOPPED";
        if (wasActive) {
            try {
                loopLeaseService.stopOwnedSessions(instanceIdentity.value(), clock.instant());
            } catch (RuntimeException exception) {
                log.warn("调度实例正常停止时回收循环会话失败，等待租约过期", exception);
            }
        }
    }

    @Override
    public void stop(Runnable callback) {
        try {
            stop();
        } finally {
            callback.run();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public int getPhase() {
        return LIFECYCLE_PHASE;
    }

    /** 当前是否允许调度内核继续派发。 */
    public boolean isReady() {
        return ready;
    }

    /**
     * 健康检查或运维恢复流程触发一次启动尝试。
     * <p>数据库短暂故障时，后续健康检查可以重新执行完整启动门禁。</p>
     */
    public void retryStartIfNeeded() {
        if (!ready) {
            start();
        }
    }

    /** 当前启动门禁状态。 */
    public String getState() {
        return state;
    }

    /** 当前不可用原因码；就绪时为 null。 */
    public String getFailureCode() {
        return failureCode;
    }

    /** 启动时解析的系统时区。 */
    public ZoneId getSystemZone() {
        return systemZone;
    }

    /** 注册处理器数量。 */
    public int registeredHandlerCount() {
        return registry.descriptors().size();
    }

    /** 数据库健康检查失败时撤销调度就绪状态。 */
    public synchronized void markDatabaseUnavailable() {
        setNotReady("DATABASE_UNAVAILABLE");
    }

    private synchronized void runTick() {
        if (!ready) {
            return;
        }
        schedulerTick.ifPresent(tick -> {
            try {
                tick.run();
            } catch (DataAccessException exception) {
                markDatabaseUnavailable();
                log.error("调度器 tick 访问数据库失败，撤销调度就绪状态", exception);
            } catch (RuntimeException exception) {
                log.error("调度内核 tick 失败", exception);
            }
        });
    }

    private void synchronizeDefinitions(List<SchedulerJobEntity> jobs) {
        for (var job : jobs) {
            if (job.getDefinitionStatus() == SchedulerDefinitionStatus.ARCHIVED) {
                continue;
            }
            var descriptor = registry.find(job.getJobKey());
            if (descriptor.isEmpty()) {
                if (job.getDefinitionStatus() != SchedulerDefinitionStatus.UNAVAILABLE) {
                    updateDefinitionState(job, SchedulerDefinitionStatus.UNAVAILABLE,
                            "标记调度任务不可用失败: " + job.getJobKey());
                }
                continue;
            }
            assertCompatible(job, descriptor.get());
            if (job.getDefinitionStatus() == SchedulerDefinitionStatus.UNAVAILABLE) {
                updateDefinitionState(job, SchedulerDefinitionStatus.REGISTERED,
                        "恢复调度任务注册状态失败: " + job.getJobKey());
            }
        }
    }

    private void updateDefinitionState(SchedulerJobEntity job,
                                       SchedulerDefinitionStatus definitionStatus,
                                       String failureMessage) {
        if (jobMapper.updateDefinitionState(job.getId(), job.getVersion(), definitionStatus.name(),
                job.getDesiredState().name()) != 1) {
            throw new IllegalStateException(failureMessage);
        }
        job.setDefinitionStatus(definitionStatus);
        job.setVersion(job.getVersion() + 1);
    }

    private void initializeNextFire(List<SchedulerJobEntity> jobs, ZoneId zone) {
        for (var job : jobs) {
            if (job.getDefinitionStatus() != SchedulerDefinitionStatus.REGISTERED
                    || job.getJobType() == ScheduledJobType.LOOP
                    || !isEnabled(job)
                    || job.getNextFireAt() != null
                    || job.getScheduleKind() == ScheduledScheduleKind.MANUAL) {
                continue;
            }
            var nextFireAt = switch (job.getScheduleKind()) {
                case CRON -> nextCronFire(job.getCronExpression(), zone, clock.instant());
                case FIXED_DELAY -> clock.instant().plusMillis(Optional.ofNullable(job.getInitialDelayMs()).orElse(0L));
                case MANUAL -> null;
            };
            if (nextFireAt != null && jobMapper.advanceNextFire(job.getId(), job.getVersion(), nextFireAt) != 1) {
                throw new IllegalStateException("初始化调度任务下一次计划失败: " + job.getJobKey());
            }
        }
    }

    private static boolean isEnabled(SchedulerJobEntity job) {
        return job.getDesiredState() == SchedulerDesiredState.ENABLED;
    }

    private static Instant nextCronFire(String expression, ZoneId zone, Instant nowInstant) {
        if (expression == null || expression.isBlank()) {
            throw new IllegalStateException("Cron 任务缺少表达式");
        }
        CronExpression cron = CronExpression.parse(expression);
        ZonedDateTime now = ZonedDateTime.ofInstant(nowInstant, zone);
        ZonedDateTime next = cron.next(now);
        if (next == null) {
            throw new IllegalStateException("Cron 表达式没有下一次计划: " + expression);
        }
        return next.toInstant();
    }

    private static void assertCompatible(SchedulerJobEntity job,
                                         com.devops00.spectra.common.scheduler.ScheduledJobDescriptor descriptor) {
        if (job.getJobType() != descriptor.jobType()
                || job.getRunScope() != descriptor.runScope()
                || job.getScheduleKind() != descriptor.scheduleKind()
                || !descriptor.handlerKey().equals(job.getHandlerKey())) {
            throw new IllegalStateException("数据库任务定义与代码注册不一致: " + job.getJobKey());
        }
    }

    private void validateProperties() {
        if (properties.getPollInterval() == null
                || properties.getPollInterval().isZero()
                || properties.getPollInterval().isNegative()) {
            throw new IllegalStateException("spectra.scheduler.poll-interval 必须大于 0");
        }
        if (properties.getDueBatchSize() <= 0) {
            throw new IllegalStateException("spectra.scheduler.due-batch-size 必须大于 0");
        }
    }

    private void setNotReady(String reason) {
        cancelTick();
        ready = false;
        running = false;
        failureCode = reason;
        state = "NOT_READY";
    }

    private void cancelTick() {
        var future = tickFuture;
        if (future != null) {
            future.cancel(false);
            tickFuture = null;
        }
    }
}

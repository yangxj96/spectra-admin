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

import com.devops00.spectra.common.scheduler.ScheduledLoopContext;
import com.devops00.spectra.common.scheduler.ScheduledLoopCycleResult;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.core.scheduler.configuration.SchedulerProperties;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerJobEntity;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerLoopRuntimeEntity;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerCommandStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDesiredState;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerRuntimeStatus;
import com.devops00.spectra.core.scheduler.mapper.SchedulerJobMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerLoopRuntimeMapper;
import com.devops00.spectra.core.scheduler.service.LoopErrorAggregator;
import com.devops00.spectra.core.scheduler.service.LoopStateMachine;
import com.devops00.spectra.core.scheduler.service.ScheduledJobRegistry;
import com.devops00.spectra.core.scheduler.service.SchedulerControlCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** 高频循环会话控制器；正常周期只更新会话计数，不创建执行记录。 */
@Slf4j
@Component
public class LoopController {

    private static final Duration DEFAULT_ERROR_LOG_INTERVAL = Duration.ofMinutes(1);
    private static final Duration DEFAULT_DRAIN_TIMEOUT = Duration.ofSeconds(60);

    private final SchedulerJobMapper jobMapper;
    private final SchedulerLoopRuntimeMapper runtimeMapper;
    private final ScheduledJobRegistry registry;
    private final SchedulerControlCommandService commandService;
    private final SingletonLoopLeaseService leaseService;
    private final LoopErrorAggregator errorAggregator;
    private final LoopStateMachine stateMachine;
    private final SchedulerProperties properties;
    private final SchedulerInstanceIdentity instanceIdentity;
    private final Clock clock;

    @Autowired
    public LoopController(SchedulerJobMapper jobMapper,
                          SchedulerLoopRuntimeMapper runtimeMapper,
                          ScheduledJobRegistry registry,
                          SchedulerControlCommandService commandService,
                          SingletonLoopLeaseService leaseService,
                          LoopErrorAggregator errorAggregator,
                          LoopStateMachine stateMachine,
                          SchedulerProperties properties,
                          SchedulerInstanceIdentity instanceIdentity) {
        this(jobMapper, runtimeMapper, registry, commandService, leaseService, errorAggregator,
                stateMachine, properties, instanceIdentity, Clock.systemUTC());
    }

    LoopController(SchedulerJobMapper jobMapper,
                   SchedulerLoopRuntimeMapper runtimeMapper,
                   ScheduledJobRegistry registry,
                   SchedulerControlCommandService commandService,
                   SingletonLoopLeaseService leaseService,
                   LoopErrorAggregator errorAggregator,
                   LoopStateMachine stateMachine,
                   SchedulerProperties properties,
                   SchedulerInstanceIdentity instanceIdentity,
                   Clock clock) {
        this.jobMapper = jobMapper;
        this.runtimeMapper = runtimeMapper;
        this.registry = registry;
        this.commandService = commandService;
        this.leaseService = leaseService;
        this.errorAggregator = errorAggregator;
        this.stateMachine = stateMachine;
        this.properties = properties;
        this.instanceIdentity = instanceIdentity;
        this.clock = clock;
    }

    /** 应用控制命令，然后推进本实例拥有的循环会话。 */
    public void runOnce() {
        var now = clock.instant();
        applyPendingCommands(now);
        for (var job : jobMapper.selectList(null)) {
            if (job.getJobType() != ScheduledJobType.LOOP
                    || job.getDefinitionStatus() == null
                    || job.getDefinitionStatus().name().equals("REGISTERED") == false) {
                continue;
            }
            runJob(job, now);
        }
    }

    private void applyPendingCommands(Instant now) {
        List<com.devops00.spectra.core.scheduler.javabean.entity.SchedulerControlCommandEntity> commands = commandService
                .pending(properties.getDueBatchSize());
        if (commands == null) {
            return;
        }
        for (var command : commands) {
            if (!commandService.claim(command)) {
                continue;
            }
            try {
                if (command.getDeadlineAt() != null && !command.getDeadlineAt().isAfter(now)) {
                    commandService.markFinished(command, SchedulerCommandStatus.TIMEOUT, now,
                            "COMMAND_TIMEOUT", "控制命令超过截止时间");
                    continue;
                }
                var result = commandService.apply(command, now, instanceIdentity.value());
                if (!commandService.markApplied(command, now, result.resultCode(), result.resultMessage())) {
                    log.warn("控制命令完成 CAS 失败: commandId={}", command.getId());
                }
            } catch (DataAccessException exception) {
                throw exception;
            } catch (RuntimeException exception) {
                if (!commandService.markFinished(command, SchedulerCommandStatus.FAILED, now,
                        "COMMAND_REJECTED", exception.getMessage())) {
                    log.warn("控制命令失败状态 CAS 失败: commandId={}", command.getId());
                }
            }
        }
    }

    private void runJob(SchedulerJobEntity job, Instant now) {
        if (job.getDesiredState() == SchedulerDesiredState.STOPPED) {
            stopOwnedRuntime(job, now, "任务期望停止");
            return;
        }
        if (job.getDesiredState() == SchedulerDesiredState.DRAINING) {
            drainOwnedRuntime(job, now);
            return;
        }
        var runtime = leaseService.ensureSession(job, instanceIdentity.value(), now).orElse(null);
        if (runtime == null) {
            return;
        }
        if (runtime.getStatus() == SchedulerRuntimeStatus.DRAINING) {
            if (runtime.getDrainDeadlineAt() != null && !runtime.getDrainDeadlineAt().isAfter(now)) {
                stopRuntime(runtime, now, "排空截止时间到达");
            } else {
                heartbeat(runtime, job, now);
            }
            return;
        }
        if (runtime.getStatus() != SchedulerRuntimeStatus.RUNNING
                && runtime.getStatus() != SchedulerRuntimeStatus.DEGRADED) {
            return;
        }
        if (!isCycleDue(job, runtime, now)) {
            heartbeat(runtime, job, now);
            return;
        }
        if (!heartbeat(runtime, job, now)) {
            return;
        }
        runCycle(job, runtime, now);
    }

    private void runCycle(SchedulerJobEntity job, SchedulerLoopRuntimeEntity runtime, Instant startedAt) {
        var handler = registry.findLoopHandler(job.getJobKey()).orElse(null);
        if (handler == null) {
            return;
        }
        var leaseDuration = leaseService.leaseDuration(job);
        ScheduledLoopCycleResult result;
        try {
            result = handler.runCycle(ScheduledLoopContext.builder()
                    .runtimeId(runtime.getId())
                    .jobKey(job.getJobKey())
                    .handlerKey(job.getHandlerKey())
                    .jobRevision(job.getRevision())
                    .handlerVersion("1.0.0")
                    .sessionKey(runtime.getSessionKey())
                    .instanceId(runtime.getInstanceId())
                    .startedAt(runtime.getStartedAt())
                    .deadline(startedAt.plus(leaseDuration))
                    .parameters(job.getParameters())
                    .drainRequested(false)
                    .build());
            if (result == null) {
                result = ScheduledLoopCycleResult.builder()
                        .failed(1)
                        .errorCode("NULL_LOOP_RESULT")
                        .sanitizedMessage("循环处理器未返回周期结果")
                        .context(Map.of())
                        .build();
            }
        } catch (RuntimeException exception) {
            result = ScheduledLoopCycleResult.builder()
                    .failed(1)
                    .errorCode("LOOP_HANDLER_EXCEPTION")
                    .sanitizedMessage("循环处理器异常")
                    .context(Map.of())
                    .build();
            log.error("循环处理器执行异常: jobKey={}, runtimeId={}", job.getJobKey(), runtime.getId(), exception);
        }
        var hasError = result.errorCode() != null && !result.errorCode().isBlank() || result.failed() > 0;
        var errorCode = hasError ? (result.errorCode() == null ? "LOOP_CYCLE_FAILED" : result.errorCode()) : null;
        var errorMessage = hasError ? result.sanitizedMessage() : null;
        if (hasError) {
            errorAggregator.record(job.getId(), runtime.getId(), runtime.getInstanceId(), errorCode,
                    errorMessage, result.context(), errorLogInterval(job), startedAt);
        }
        var targetStatus = hasError ? SchedulerRuntimeStatus.DEGRADED : SchedulerRuntimeStatus.RUNNING;
        var consecutiveErrors = hasError
                ? Math.max(0L, runtime.getConsecutiveErrorCount() == null ? 0L : runtime.getConsecutiveErrorCount()) + 1
                : 0L;
        if (runtimeMapper.recordCycle(runtime.getId(), runtime.getVersion(), runtime.getInstanceId(),
                targetStatus.name(), startedAt, result.processed() > 0 ? startedAt : null,
                result.processed(), result.failed(), consecutiveErrors, errorCode, errorMessage) != 1) {
            log.warn("循环周期结果 CAS 失败，不修改其他会话状态: runtimeId={}", runtime.getId());
        }
    }

    private boolean heartbeat(SchedulerLoopRuntimeEntity runtime, SchedulerJobEntity job, Instant now) {
        var leaseExpiresAt = now.plus(leaseService.leaseDuration(job));
        if (runtimeMapper.heartbeatRuntime(runtime.getId(), runtime.getVersion(), runtime.getInstanceId(),
                now, leaseExpiresAt) != 1) {
            return false;
        }
        runtime.setLastHeartbeatAt(now);
        runtime.setLeaseExpiresAt(leaseExpiresAt);
        runtime.setVersion(runtime.getVersion() + 1);
        return true;
    }

    private void drainRuntime(SchedulerLoopRuntimeEntity runtime, Instant now) {
        if (runtime.getStatus() == SchedulerRuntimeStatus.DRAINING) {
            return;
        }
        stateMachine.transition(runtime.getStatus(), SchedulerRuntimeStatus.DRAINING);
        runtimeMapper.transitionRuntime(runtime.getId(), runtime.getVersion(), runtime.getInstanceId(),
                SchedulerRuntimeStatus.DRAINING.name(), "任务期望排空停止", now.plus(DEFAULT_DRAIN_TIMEOUT), null);
    }

    private void drainOwnedRuntime(SchedulerJobEntity job, Instant now) {
        for (var runtime : runtimeMapper.selectByJobId(job.getId())) {
            if (runtime.getInstanceId().equals(instanceIdentity.value()) && isActive(runtime)) {
                if (runtime.getStatus() == SchedulerRuntimeStatus.DRAINING
                        && runtime.getDrainDeadlineAt() != null
                        && !runtime.getDrainDeadlineAt().isAfter(now)) {
                    stopRuntime(runtime, now, "排空截止时间到达");
                } else {
                    drainRuntime(runtime, now);
                }
            }
        }
    }

    private void stopOwnedRuntime(SchedulerJobEntity job, Instant now, String reason) {
        for (var runtime : runtimeMapper.selectByJobId(job.getId())) {
            if (runtime.getInstanceId().equals(instanceIdentity.value()) && isActive(runtime)) {
                stopRuntime(runtime, now, reason);
            }
        }
    }

    private void stopRuntime(SchedulerLoopRuntimeEntity runtime, Instant now, String reason) {
        if (runtime.getStatus() == SchedulerRuntimeStatus.STOPPED) {
            return;
        }
        if (stateMachine.transition(runtime.getStatus(), SchedulerRuntimeStatus.STOPPED) != SchedulerRuntimeStatus.STOPPED) {
            return;
        }
        runtimeMapper.transitionRuntime(runtime.getId(), runtime.getVersion(), runtime.getInstanceId(),
                SchedulerRuntimeStatus.STOPPED.name(), reason, null, now);
    }

    private static boolean isCycleDue(SchedulerJobEntity job, SchedulerLoopRuntimeEntity runtime, Instant now) {
        var delay = job.getFixedDelayMs() == null ? 1000L : job.getFixedDelayMs();
        if (delay <= 0) {
            throw new IllegalStateException("LOOP fixedDelayMs 必须大于 0: " + job.getJobKey());
        }
        if (runtime.getLastCycleAt() == null) {
            var initialDelay = job.getInitialDelayMs() == null ? 0L : job.getInitialDelayMs();
            return !now.isBefore(runtime.getStartedAt().plusMillis(initialDelay));
        }
        return !now.isBefore(runtime.getLastCycleAt().plusMillis(delay));
    }

    private static Duration errorLogInterval(SchedulerJobEntity job) {
        Object value = job.getExecutionPolicy() == null ? null : job.getExecutionPolicy().get("errorLogIntervalMs");
        if (value == null) {
            return DEFAULT_ERROR_LOG_INTERVAL;
        }
        try {
            var millis = Long.parseLong(String.valueOf(value));
            if (millis <= 0) {
                throw new IllegalStateException("errorLogIntervalMs 必须大于 0");
            }
            return Duration.ofMillis(millis);
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("errorLogIntervalMs 必须是正整数毫秒数", exception);
        }
    }

    private static boolean isActive(SchedulerLoopRuntimeEntity runtime) {
        return runtime.getStatus() == SchedulerRuntimeStatus.STARTING
                || runtime.getStatus() == SchedulerRuntimeStatus.RUNNING
                || runtime.getStatus() == SchedulerRuntimeStatus.DEGRADED
                || runtime.getStatus() == SchedulerRuntimeStatus.DRAINING;
    }
}

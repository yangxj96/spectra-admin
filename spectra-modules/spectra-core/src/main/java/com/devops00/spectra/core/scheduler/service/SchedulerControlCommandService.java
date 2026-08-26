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

import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerControlCommandEntity;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerCommandStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerCommandType;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDesiredState;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerRuntimeStatus;
import com.devops00.spectra.core.scheduler.mapper.SchedulerJobMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerControlCommandMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerLoopRuntimeMapper;
import com.devops00.spectra.core.scheduler.worker.SingletonLoopLeaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** 控制命令的持久化、幂等和命令级 CAS。 */
@Service
public class SchedulerControlCommandService {

    private final SchedulerControlCommandMapper commandMapper;
    private final SchedulerJobMapper jobMapper;
    private final SchedulerLoopRuntimeMapper runtimeMapper;
    private final SingletonLoopLeaseService leaseService;
    private final LoopStateMachine stateMachine;
    private final Clock clock;

    /** 仅供不涉及命令应用的轻量单元测试使用。 */
    public SchedulerControlCommandService(SchedulerControlCommandMapper commandMapper) {
        this(commandMapper, null, null, null, null, Clock.systemUTC());
    }

    @Autowired
    public SchedulerControlCommandService(SchedulerControlCommandMapper commandMapper,
                                          SchedulerJobMapper jobMapper,
                                          SchedulerLoopRuntimeMapper runtimeMapper,
                                          SingletonLoopLeaseService leaseService,
                                          LoopStateMachine stateMachine) {
        this(commandMapper, jobMapper, runtimeMapper, leaseService, stateMachine, Clock.systemUTC());
    }

    SchedulerControlCommandService(SchedulerControlCommandMapper commandMapper,
                                   SchedulerJobMapper jobMapper,
                                   SchedulerLoopRuntimeMapper runtimeMapper,
                                   SingletonLoopLeaseService leaseService,
                                   LoopStateMachine stateMachine,
                                   Clock clock) {
        this.commandMapper = commandMapper;
        this.jobMapper = jobMapper;
        this.runtimeMapper = runtimeMapper;
        this.leaseService = leaseService;
        this.stateMachine = stateMachine;
        this.clock = clock;
    }

    /** 先按幂等键读取，再以唯一键兜底，重复请求返回原命令。 */
    @Transactional
    public SchedulerControlCommandEntity request(UUID jobId,
                                                 SchedulerCommandType commandType,
                                                 UUID targetRuntimeId,
                                                 String targetSessionKey,
                                                 Long expectedRuntimeVersion,
                                                 String idempotencyKey,
                                                 String reason,
                                                 UUID requestedBy,
                                                 Instant requestedAt,
                                                 Instant deadlineAt) {
        validateRequest(jobId, commandType, targetRuntimeId, targetSessionKey, expectedRuntimeVersion,
                idempotencyKey, reason);
        var existing = commandMapper.selectByIdempotencyKey(idempotencyKey);
        if (existing != null) {
            return existing;
        }
        var now = requestedAt == null ? clock.instant() : requestedAt;
        var command = new SchedulerControlCommandEntity();
        command.setId(UUID.randomUUID());
        command.setJobId(jobId);
        command.setTargetRuntimeId(targetRuntimeId);
        command.setTargetSessionKey(targetSessionKey);
        command.setExpectedRuntimeVersion(expectedRuntimeVersion);
        command.setCommandType(commandType);
        command.setStatus(SchedulerCommandStatus.REQUESTED);
        command.setIdempotencyKey(idempotencyKey.trim());
        command.setReason(sanitizeReason(reason));
        command.setRequestedBy(requestedBy);
        command.setRequestedAt(now);
        command.setDeadlineAt(deadlineAt);
        command.setCreatedAt(now);
        command.setUpdatedAt(now);
        command.setVersion(0L);
        if (commandMapper.insertIfAbsent(command) == 1) {
            return command;
        }
        existing = commandMapper.selectByIdempotencyKey(idempotencyKey);
        if (existing == null) {
            throw new IllegalStateException("控制命令幂等插入失败且无法读取原命令");
        }
        return existing;
    }

    /** 查询待应用命令。 */
    public List<SchedulerControlCommandEntity> pending(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("控制命令批次必须大于 0");
        }
        return commandMapper.selectPendingCommands(limit);
    }

    /** 领取 REQUESTED 命令，返回领取后的版本。 */
    public boolean claim(SchedulerControlCommandEntity command) {
        if (command == null || command.getId() == null || command.getVersion() == null) {
            throw new IllegalArgumentException("控制命令缺少版本或 ID");
        }
        var claimed = commandMapper.claimCommand(command.getId(), command.getVersion()) == 1;
        if (claimed) {
            command.setStatus(SchedulerCommandStatus.APPLYING);
            command.setVersion(command.getVersion() + 1);
        }
        return claimed;
    }

    /** 以命令版本标记已应用。 */
    public boolean markApplied(SchedulerControlCommandEntity command, Instant appliedAt,
                               String resultCode, String resultMessage) {
        return command != null
                && command.getVersion() != null
                && commandMapper.markApplied(command.getId(), command.getVersion(),
                        appliedAt == null ? clock.instant() : appliedAt, safeCode(resultCode), safeMessage(resultMessage)) == 1;
    }

    /** 以命令版本标记失败或超时。 */
    public boolean markFinished(SchedulerControlCommandEntity command, SchedulerCommandStatus status,
                                Instant finishedAt, String resultCode, String resultMessage) {
        if (status != SchedulerCommandStatus.FAILED && status != SchedulerCommandStatus.TIMEOUT) {
            throw new IllegalArgumentException("命令完成状态只能是 FAILED 或 TIMEOUT");
        }
        return command != null
                && command.getVersion() != null
                && commandMapper.markFinished(command.getId(), command.getVersion(), status.name(),
                        finishedAt == null ? clock.instant() : finishedAt, safeCode(resultCode), safeMessage(resultMessage)) == 1;
    }

    /**
     * 在一个短事务中应用已领取的命令。运行周期本身不在该事务内执行。
     */
    @Transactional
    public CommandApplication apply(SchedulerControlCommandEntity command, Instant appliedAt, String instanceId) {
        if (command == null
                || command.getJobId() == null
                || command.getCommandType() == null
                || instanceId == null
                || instanceId.isBlank()
                || jobMapper == null
                || runtimeMapper == null) {
            throw new IllegalArgumentException("控制命令应用参数不完整");
        }
        var now = appliedAt == null ? clock.instant() : appliedAt;
        var job = jobMapper.selectByIdForUpdate(command.getJobId());
        if (job == null || job.getJobType() == null || job.getJobType().name().equals("LOOP") == false) {
            throw new IllegalStateException("控制命令目标不是可控制的 LOOP 任务");
        }
        return switch (command.getCommandType()) {
            case START -> applyStart(command, job, now, instanceId);
            case DRAIN_STOP -> applyTransition(command, job, now, SchedulerRuntimeStatus.DRAINING,
                    SchedulerDesiredState.DRAINING, command.getDeadlineAt() == null
                            ? now.plusSeconds(60)
                            : command.getDeadlineAt());
            case FORCE_STOP -> applyTransition(command, job, now, SchedulerRuntimeStatus.STOPPED,
                    SchedulerDesiredState.STOPPED, null);
            case RESTART -> applyRestart(command, job, now);
            case FORCE_RECLAIM -> applyReclaim(command, now);
        };
    }

    private CommandApplication applyStart(SchedulerControlCommandEntity command,
                                          com.devops00.spectra.core.scheduler.javabean.entity.SchedulerJobEntity job,
                                          Instant now, String instanceId) {
        updateDesiredState(job, SchedulerDesiredState.RUNNING);
        var session = leaseService.ensureSession(job, instanceId, now);
        if (session.isEmpty()) {
            throw new IllegalStateException("LOOP 单例租约正在被其他实例持有");
        }
        return new CommandApplication("STARTED", "循环已启动");
    }

    private CommandApplication applyTransition(
                                               SchedulerControlCommandEntity command,
                                               com.devops00.spectra.core.scheduler.javabean.entity.SchedulerJobEntity job,
                                               Instant now,
                                               SchedulerRuntimeStatus targetStatus,
                                               SchedulerDesiredState desiredState,
                                               Instant drainDeadlineAt) {
        var runtime = targetRuntime(command);
        stateMachine.transition(runtime.getStatus(), targetStatus);
        var updated = runtimeMapper.transitionRuntime(runtime.getId(), runtime.getVersion(), runtime.getInstanceId(),
                targetStatus.name(), command.getReason(), drainDeadlineAt,
                targetStatus == SchedulerRuntimeStatus.STOPPED ? now : null);
        if (updated != 1) {
            throw new IllegalStateException("控制命令目标会话已发生变化");
        }
        updateDesiredState(job, desiredState);
        return new CommandApplication(targetStatus.name(), "循环状态已切换为 " + targetStatus.name());
    }

    private CommandApplication applyRestart(SchedulerControlCommandEntity command,
                                            com.devops00.spectra.core.scheduler.javabean.entity.SchedulerJobEntity job,
                                            Instant now) {
        var runtime = targetRuntime(command);
        stateMachine.transition(runtime.getStatus(), SchedulerRuntimeStatus.STOPPED);
        if (runtimeMapper.transitionRuntime(runtime.getId(), runtime.getVersion(), runtime.getInstanceId(),
                SchedulerRuntimeStatus.STOPPED.name(), command.getReason(), null, now) != 1) {
            throw new IllegalStateException("重启目标会话已发生变化");
        }
        updateDesiredState(job, SchedulerDesiredState.RUNNING);
        return new CommandApplication("RESTART_REQUESTED", "旧循环会话已停止，将创建新会话");
    }

    private CommandApplication applyReclaim(SchedulerControlCommandEntity command, Instant now) {
        var runtime = targetRuntime(command);
        if (runtime.getLeaseExpiresAt() == null || !runtime.getLeaseExpiresAt().isBefore(now)) {
            throw new IllegalStateException("只有已确认过期的循环租约才能强制回收");
        }
        if (runtimeMapper.reclaimExpiredRuntime(runtime.getId(), runtime.getVersion(),
                SchedulerRuntimeStatus.CRASHED.name(), command.getReason(), now) != 1) {
            throw new IllegalStateException("循环租约回收 CAS 失败");
        }
        return new CommandApplication("RECLAIMED", "过期循环租约已回收");
    }

    private com.devops00.spectra.core.scheduler.javabean.entity.SchedulerLoopRuntimeEntity targetRuntime(
                                                                                                         SchedulerControlCommandEntity command) {
        var runtime = runtimeMapper.selectById(command.getTargetRuntimeId());
        if (runtime == null
                || runtime.getJobId() == null
                || !command.getJobId().equals(runtime.getJobId())
                || !command.getTargetSessionKey().equals(runtime.getSessionKey())
                || runtime.getVersion() == null
                || runtime.getVersion() < command.getExpectedRuntimeVersion()) {
            throw new IllegalStateException("控制命令目标会话已过期");
        }
        return runtime;
    }

    private void updateDesiredState(
                                    com.devops00.spectra.core.scheduler.javabean.entity.SchedulerJobEntity job,
                                    SchedulerDesiredState desiredState) {
        if (jobMapper.updateDesiredState(job.getId(), job.getVersion(), desiredState.name()) != 1) {
            throw new IllegalStateException("更新 LOOP 期望状态失败: " + job.getJobKey());
        }
    }

    private static void validateRequest(UUID jobId,
                                        SchedulerCommandType commandType,
                                        UUID targetRuntimeId,
                                        String targetSessionKey,
                                        Long expectedRuntimeVersion,
                                        String idempotencyKey,
                                        String reason) {
        if (jobId == null
                || commandType == null
                || idempotencyKey == null
                || idempotencyKey.isBlank()
                || reason == null
                || reason.isBlank()) {
            throw new IllegalArgumentException("控制命令缺少任务、类型、幂等键或原因");
        }
        boolean hasTarget = targetRuntimeId != null || targetSessionKey != null || expectedRuntimeVersion != null;
        if (commandType == SchedulerCommandType.START && hasTarget) {
            throw new IllegalArgumentException("START 命令不接受旧会话目标");
        }
        if (commandType != SchedulerCommandType.START
                && (targetRuntimeId == null
                        || targetSessionKey == null
                        || targetSessionKey.isBlank()
                        || expectedRuntimeVersion == null
                        || expectedRuntimeVersion < 0)) {
            throw new IllegalArgumentException("非 START 命令必须指定目标会话和版本");
        }
    }

    private static String sanitizeReason(String reason) {
        return safeMessage(reason);
    }

    private static String safeCode(String code) {
        return code == null || code.isBlank() ? null : code.trim().replaceAll("[^A-Za-z0-9_.:-]", "_");
    }

    private static String safeMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        var sanitized = message.replaceAll("[\\r\\n\\t]+", " ").trim();
        return sanitized.length() <= 500 ? sanitized : sanitized.substring(0, 500);
    }

    /** 命令应用结果。 */
    public record CommandApplication(String resultCode, String resultMessage) {
    }
}

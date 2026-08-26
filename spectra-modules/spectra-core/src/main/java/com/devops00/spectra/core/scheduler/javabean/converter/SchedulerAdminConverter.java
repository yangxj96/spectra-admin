package com.devops00.spectra.core.scheduler.javabean.converter;

import com.devops00.spectra.common.scheduler.ScheduledJobDescriptor;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerControlCommandEntity;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerExecutionEntity;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerJobEntity;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerLoopErrorEntity;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerLoopRuntimeEntity;
import com.devops00.spectra.core.scheduler.javabean.domain.SchedulerOperationHistoryRow;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerCatalogVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerControlCommandVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerExecutionVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerJobVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerLoopErrorVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerLoopRuntimeVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerOperationVO;

import java.util.List;
import java.util.Map;

/** 调度管理实体和视图转换器。 */
public final class SchedulerAdminConverter {

    private SchedulerAdminConverter() {
    }

    public static SchedulerCatalogVO toCatalog(ScheduledJobDescriptor descriptor) {
        return SchedulerCatalogVO.builder()
                .jobKey(descriptor.jobKey())
                .handlerKey(descriptor.handlerKey())
                .name(descriptor.name())
                .module(descriptor.module())
                .jobType(descriptor.jobType())
                .runScope(descriptor.runScope())
                .scheduleKind(descriptor.scheduleKind())
                .effectType(descriptor.effectType())
                .parameterSchema(copy(descriptor.parameterSchema()))
                .supportedActions(descriptor.supportedActions().stream().sorted().toList())
                .executionPolicy(copy(descriptor.executionPolicy()))
                .build();
    }

    public static SchedulerJobVO toJob(SchedulerJobEntity entity) {
        return SchedulerJobVO.builder()
                .id(entity.getId())
                .jobKey(entity.getJobKey())
                .name(entity.getName())
                .module(entity.getModule())
                .description(entity.getDescription())
                .handlerKey(entity.getHandlerKey())
                .jobType(entity.getJobType())
                .runScope(entity.getRunScope())
                .definitionStatus(entity.getDefinitionStatus())
                .desiredState(entity.getDesiredState())
                .scheduleKind(entity.getScheduleKind())
                .cronExpression(entity.getCronExpression())
                .fixedDelayMs(entity.getFixedDelayMs())
                .initialDelayMs(entity.getInitialDelayMs())
                .nextFireAt(entity.getNextFireAt())
                .misfirePolicy(entity.getMisfirePolicy())
                .concurrencyPolicy(entity.getConcurrencyPolicy())
                .executionPolicy(copy(entity.getExecutionPolicy()))
                .parameters(copy(entity.getParameters()))
                .revision(entity.getRevision())
                .version(entity.getVersion())
                .build();
    }

    public static SchedulerExecutionVO toExecution(SchedulerExecutionEntity entity) {
        return SchedulerExecutionVO.builder()
                .id(entity.getId())
                .jobId(entity.getJobId())
                .fireKey(entity.getFireKey())
                .triggerType(entity.getTriggerType())
                .status(entity.getStatus())
                .jobRevision(entity.getJobRevision())
                .handlerVersion(entity.getHandlerVersion())
                .scheduleKindSnapshot(entity.getScheduleKindSnapshot())
                .scheduleExpressionSnapshot(entity.getScheduleExpressionSnapshot())
                .parametersSnapshot(copy(entity.getParametersSnapshot()))
                .effectType(entity.getEffectType())
                .scheduledAt(entity.getScheduledAt())
                .queuedAt(entity.getQueuedAt())
                .startedAt(entity.getStartedAt())
                .finishedAt(entity.getFinishedAt())
                .nextRetryAt(entity.getNextRetryAt())
                .deadlineAt(entity.getDeadlineAt())
                .attemptNo(entity.getAttemptNo())
                .maxAttempts(entity.getMaxAttempts())
                .lockedBy(entity.getLockedBy())
                .lockedAt(entity.getLockedAt())
                .leaseExpiresAt(entity.getLeaseExpiresAt())
                .lastHeartbeatAt(entity.getLastHeartbeatAt())
                .lastErrorCode(entity.getLastErrorCode())
                .lastErrorMessage(entity.getLastErrorMessage())
                .resultSummary(copy(entity.getResultSummary()))
                .originalExecutionId(entity.getOriginalExecutionId())
                .resolutionStatus(entity.getResolutionStatus())
                .resolutionReason(entity.getResolutionReason())
                .resolvedBy(entity.getResolvedBy())
                .resolvedAt(entity.getResolvedAt())
                .version(entity.getVersion())
                .build();
    }

    public static SchedulerLoopRuntimeVO toRuntime(SchedulerLoopRuntimeEntity entity) {
        return SchedulerLoopRuntimeVO.builder()
                .id(entity.getId())
                .jobId(entity.getJobId())
                .sessionKey(entity.getSessionKey())
                .instanceId(entity.getInstanceId())
                .status(entity.getStatus())
                .startedAt(entity.getStartedAt())
                .stoppedAt(entity.getStoppedAt())
                .lastHeartbeatAt(entity.getLastHeartbeatAt())
                .leaseExpiresAt(entity.getLeaseExpiresAt())
                .lastCycleAt(entity.getLastCycleAt())
                .lastProgressAt(entity.getLastProgressAt())
                .drainDeadlineAt(entity.getDrainDeadlineAt())
                .totalCycles(entity.getTotalCycles())
                .totalProcessed(entity.getTotalProcessed())
                .totalFailed(entity.getTotalFailed())
                .consecutiveErrorCount(entity.getConsecutiveErrorCount())
                .lastErrorCode(entity.getLastErrorCode())
                .lastErrorMessage(entity.getLastErrorMessage())
                .stateReason(entity.getStateReason())
                .version(entity.getVersion())
                .build();
    }

    public static SchedulerLoopErrorVO toError(SchedulerLoopErrorEntity entity) {
        return SchedulerLoopErrorVO.builder()
                .id(entity.getId())
                .jobId(entity.getJobId())
                .instanceId(entity.getInstanceId())
                .runtimeId(entity.getRuntimeId())
                .errorFingerprint(entity.getErrorFingerprint())
                .errorCode(entity.getErrorCode())
                .errorMessage(entity.getErrorMessage())
                .status(entity.getStatus())
                .firstSeenAt(entity.getFirstSeenAt())
                .lastSeenAt(entity.getLastSeenAt())
                .lastLoggedAt(entity.getLastLoggedAt())
                .occurrenceCount(entity.getOccurrenceCount())
                .suppressedCount(entity.getSuppressedCount())
                .lastContext(copy(entity.getLastContext()))
                .resolvedBy(entity.getResolvedBy())
                .resolvedAt(entity.getResolvedAt())
                .resolutionReason(entity.getResolutionReason())
                .version(entity.getVersion())
                .build();
    }

    public static SchedulerControlCommandVO toCommand(SchedulerControlCommandEntity entity) {
        return SchedulerControlCommandVO.builder()
                .id(entity.getId())
                .jobId(entity.getJobId())
                .targetRuntimeId(entity.getTargetRuntimeId())
                .targetSessionKey(entity.getTargetSessionKey())
                .expectedRuntimeVersion(entity.getExpectedRuntimeVersion())
                .commandType(entity.getCommandType())
                .status(entity.getStatus())
                .idempotencyKey(entity.getIdempotencyKey())
                .reason(entity.getReason())
                .requestedBy(entity.getRequestedBy())
                .requestedAt(entity.getRequestedAt())
                .deadlineAt(entity.getDeadlineAt())
                .appliedAt(entity.getAppliedAt())
                .finishedAt(entity.getFinishedAt())
                .resultCode(entity.getResultCode())
                .resultMessage(entity.getResultMessage())
                .version(entity.getVersion())
                .build();
    }

    public static SchedulerOperationVO toOperation(SchedulerOperationHistoryRow row) {
        return SchedulerOperationVO.builder()
                .id(row.getId())
                .jobId(row.getJobId())
                .executionId(row.getExecutionId())
                .operationType(row.getOperationType())
                .source(row.getSource())
                .status(row.getStatus())
                .idempotencyKey(row.getIdempotencyKey())
                .reason(row.getReason())
                .requestedBy(row.getRequestedBy())
                .requestedAt(row.getRequestedAt())
                .finishedAt(row.getFinishedAt())
                .resultCode(row.getResultCode())
                .resultMessage(row.getResultMessage())
                .build();
    }

    private static Map<String, Object> copy(Map<String, Object> value) {
        return value == null ? Map.of() : Map.copyOf(value);
    }
}

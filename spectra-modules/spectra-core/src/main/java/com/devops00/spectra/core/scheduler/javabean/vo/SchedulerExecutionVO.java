package com.devops00.spectra.core.scheduler.javabean.vo;

import com.devops00.spectra.common.scheduler.ScheduledEffectType;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.common.scheduler.ScheduledTriggerType;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerExecutionStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerResolutionStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** 离散执行视图。 */
@Builder
public record SchedulerExecutionVO(UUID id, UUID jobId, String fireKey, ScheduledTriggerType triggerType,
                                   SchedulerExecutionStatus status, Long jobRevision, String handlerVersion,
                                   ScheduledScheduleKind scheduleKindSnapshot, String scheduleExpressionSnapshot,
                                   Map<String, Object> parametersSnapshot, ScheduledEffectType effectType,
                                   Instant scheduledAt, Instant queuedAt, Instant startedAt, Instant finishedAt,
                                   Instant nextRetryAt, Instant deadlineAt, Integer attemptNo, Integer maxAttempts,
                                   String lockedBy, Instant lockedAt, Instant leaseExpiresAt, Instant lastHeartbeatAt,
                                   String lastErrorCode, String lastErrorMessage, Map<String, Object> resultSummary,
                                   UUID originalExecutionId, SchedulerResolutionStatus resolutionStatus,
                                   String resolutionReason, UUID resolvedBy, Instant resolvedAt, Long version) {
}

package com.devops00.spectra.core.scheduler.javabean.vo;

import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerLoopErrorStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** 循环错误聚合视图。 */
@Builder
public record SchedulerLoopErrorVO(UUID id, UUID jobId, String instanceId, UUID runtimeId,
                                   String errorFingerprint, String errorCode, String errorMessage,
                                   SchedulerLoopErrorStatus status, Instant firstSeenAt, Instant lastSeenAt,
                                   Instant lastLoggedAt, Long occurrenceCount, Long suppressedCount,
                                   Map<String, Object> lastContext, UUID resolvedBy, Instant resolvedAt,
                                   String resolutionReason, Long version) {

    public SchedulerLoopErrorVO {
        lastContext = lastContext == null ? Map.of() : Map.copyOf(lastContext);
    }
}

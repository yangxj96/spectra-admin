package com.devops00.spectra.core.scheduler.javabean.vo;

import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerRuntimeStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/** 循环运行会话视图。 */
@Builder
public record SchedulerLoopRuntimeVO(UUID id, UUID jobId, String sessionKey, String instanceId,
                                     SchedulerRuntimeStatus status, Instant startedAt, Instant stoppedAt,
                                     Instant lastHeartbeatAt, Instant leaseExpiresAt, Instant lastCycleAt,
                                     Instant lastProgressAt, Instant drainDeadlineAt, Long totalCycles,
                                     Long totalProcessed, Long totalFailed, Long consecutiveErrorCount,
                                     String lastErrorCode, String lastErrorMessage, String stateReason, Long version) {
}

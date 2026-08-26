package com.devops00.spectra.core.scheduler.javabean.vo;

import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerCommandStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerCommandType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/** 循环控制命令视图。 */
@Builder
public record SchedulerControlCommandVO(UUID id, UUID jobId, UUID targetRuntimeId, String targetSessionKey,
                                        Long expectedRuntimeVersion, SchedulerCommandType commandType,
                                        SchedulerCommandStatus status, String idempotencyKey, String reason,
                                        UUID requestedBy, Instant requestedAt, Instant deadlineAt, Instant appliedAt,
                                        Instant finishedAt, String resultCode, String resultMessage, Long version) {
}

/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/** 服务监控诊断任务视图。 */
@Data
@Builder
public class ServiceMonitorDiagnosticTaskVO {

    private UUID id;
    private String taskType;
    private String status;
    private String displayName;
    private Long fileSize;
    private String errorMessage;
    private LocalDateTime requestedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime expiresAt;
}

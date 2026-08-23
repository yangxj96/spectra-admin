/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/** 服务监控告警事件视图。 */
@Data
@Builder
public class ServiceMonitorAlertEventVO {

    private UUID id;
    private UUID ruleId;
    private String ruleCode;
    private String ruleName;
    private String metricCode;
    private String severity;
    private String state;
    private String currentValue;
    private Double thresholdValue;
    private String expectedValue;
    private String message;
    private LocalDateTime firstOccurredAt;
    private LocalDateTime lastOccurredAt;
    private LocalDateTime recoveredAt;
    private Integer occurrenceCount;
    private LocalDateTime lastNotifiedAt;
}

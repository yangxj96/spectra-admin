/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/** 服务监控告警规则视图。 */
@Data
@Builder
public class ServiceMonitorAlertRuleVO {

    private UUID id;
    private String code;
    private String name;
    private String metricCode;
    private String metricLabel;
    private String operatorCode;
    private Double thresholdValue;
    private String expectedValue;
    private String severity;
    private Boolean enabled;
    private Integer consecutiveFailures;
    private Integer cooldownSeconds;
    private String remark;
    private Long version;
    private LocalDateTime updatedAt;
}

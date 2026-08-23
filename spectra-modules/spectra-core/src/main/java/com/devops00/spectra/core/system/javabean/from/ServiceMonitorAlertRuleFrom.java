/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.from;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/** 服务监控告警规则修改请求。 */
@Data
public class ServiceMonitorAlertRuleFrom {

    private UUID id;

    @NotNull(message = "规则版本不能为空")
    private Long expectedVersion;

    private String name;
    private String operatorCode;
    private Double thresholdValue;
    private String expectedValue;
    private String severity;
    private Boolean enabled;
    private Integer consecutiveFailures;
    private Integer cooldownSeconds;
    private String remark;
}

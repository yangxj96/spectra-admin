/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.system.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 单体服务监控告警规则。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_service_monitor_alert_rule", schema = "spectra_core")
public class ServiceMonitorAlertRule extends BaseEntity {

    /** 稳定业务编码。 */
    @TableField("code")
    private String code;

    /** 规则名称。 */
    @TableField("name")
    private String name;

    /** 监控指标编码。 */
    @TableField("metric_code")
    private String metricCode;

    /** 比较运算符。 */
    @TableField("operator_code")
    private String operatorCode;

    /** 数值阈值。 */
    @TableField("threshold_value")
    private Double thresholdValue;

    /** 文本期望值。 */
    @TableField("expected_value")
    private String expectedValue;

    /** 告警级别。 */
    @TableField("severity")
    private String severity;

    /** 是否启用。 */
    @TableField("enabled")
    private Boolean enabled;

    /** 连续触发次数。 */
    @TableField("consecutive_failures")
    private Integer consecutiveFailures;

    /** 通知冷却时间，单位为秒。 */
    @TableField("cooldown_seconds")
    private Integer cooldownSeconds;

    /** 规则说明。 */
    @TableField("remark")
    private String remark;
}

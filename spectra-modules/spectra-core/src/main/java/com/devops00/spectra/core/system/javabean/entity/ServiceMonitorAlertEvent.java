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

import java.time.Instant;
import java.util.UUID;

/** 单体服务监控告警事件。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_service_monitor_alert_event", schema = "spectra_core")
public class ServiceMonitorAlertEvent extends BaseEntity {

    /** 规则 ID。 */
    @TableField("rule_id")
    private UUID ruleId;

    /** 规则编码快照。 */
    @TableField("rule_code")
    private String ruleCode;

    /** 规则名称快照。 */
    @TableField("rule_name")
    private String ruleName;

    /** 指标编码。 */
    @TableField("metric_code")
    private String metricCode;

    /** 告警级别。 */
    @TableField("severity")
    private String severity;

    /** 事件状态。 */
    @TableField("state")
    private String state;

    /** 当前值。 */
    @TableField("current_value")
    private String currentValue;

    /** 数值阈值快照。 */
    @TableField("threshold_value")
    private Double thresholdValue;

    /** 文本期望值快照。 */
    @TableField("expected_value")
    private String expectedValue;

    /** 脱敏后的告警说明。 */
    @TableField("message")
    private String message;

    /** 首次触发时间。 */
    @TableField("first_occurred_at")
    private Instant firstOccurredAt;

    /** 最近触发时间。 */
    @TableField("last_occurred_at")
    private Instant lastOccurredAt;

    /** 恢复时间。 */
    @TableField("recovered_at")
    private Instant recoveredAt;

    /** 连续触发次数。 */
    @TableField("occurrence_count")
    private Integer occurrenceCount;

    /** 最近通知时间。 */
    @TableField("last_notified_at")
    private Instant lastNotifiedAt;
}

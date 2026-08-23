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

/** 服务监控诊断任务及其受控产物。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_service_monitor_diagnostic_task", schema = "spectra_core")
public class ServiceMonitorDiagnosticTask extends BaseEntity {

    /** 诊断类型。 */
    @TableField("task_type")
    private String taskType;

    /** 任务状态。 */
    @TableField("status")
    private String status;

    /** 系统生成的相对文件名，不保存绝对路径。 */
    @TableField("file_name")
    private String fileName;

    /** 前端展示名称。 */
    @TableField("display_name")
    private String displayName;

    /** 文件大小。 */
    @TableField("file_size")
    private Long fileSize;

    /** 脱敏后的失败原因。 */
    @TableField("error_message")
    private String errorMessage;

    /** 请求时间。 */
    @TableField("requested_at")
    private Instant requestedAt;

    /** 开始执行时间。 */
    @TableField("started_at")
    private Instant startedAt;

    /** 完成时间。 */
    @TableField("completed_at")
    private Instant completedAt;

    /** 文件过期时间。 */
    @TableField("expires_at")
    private Instant expiresAt;
}

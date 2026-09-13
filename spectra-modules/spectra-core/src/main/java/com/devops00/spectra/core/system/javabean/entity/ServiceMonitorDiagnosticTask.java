/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.system.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.framework.persistence.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * 服务监控诊断任务及其受控诊断产物的执行记录。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
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

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

package com.devops00.spectra.core.quartz.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.framework.persistence.base.BaseEntity;
import com.devops00.spectra.core.quartz.javabean.enums.QuartzExecutionHistoryStatus;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

/** Quartz 执行历史实体；公共主键和审计字段统一继承 framework 持久化基类。 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "quartz_job_execution_history", schema = "spectra_core")
public class QuartzJobExecutionHistoryEntity extends BaseEntity {

    /** Quartz fire instance 唯一标识。 */
    @TableField("fire_instance_id")
    private String fireInstanceId;

    /** Quartz JobKey 名称。 */
    @TableField("job_key")
    private String jobKey;

    /** Quartz TriggerKey 名称。 */
    @TableField("trigger_key")
    private String triggerKey;

    /** 代码白名单中的任务类型键。 */
    @TableField("job_type")
    private String jobType;

    /** 实际执行的受信任 Job 类名，仅用于诊断展示。 */
    @TableField("job_class_name")
    private String jobClassName;

    /** Quartz Trigger 类型。 */
    @TableField("trigger_type")
    private String triggerType;

    /** 执行历史状态。 */
    @TableField("status")
    private QuartzExecutionHistoryStatus status;

    /** Quartz 计划触发时间。 */
    @TableField("scheduled_fire_at")
    private Instant scheduledFireAt;

    /** Quartz 实际获得执行机会的时间。 */
    @TableField("actual_fire_at")
    private Instant actualFireAt;

    /** Job 开始执行时间。 */
    @TableField("started_at")
    private Instant startedAt;

    /** Job 完成或被否决的时间。 */
    @TableField("finished_at")
    private Instant finishedAt;

    /** 执行耗时，单位为毫秒。 */
    @TableField("duration_ms")
    private Long durationMs;

    /** Quartz Scheduler 实例标识。 */
    @TableField("scheduler_instance")
    private String schedulerInstance;

    /** 请求或后台任务关联标识。 */
    @TableField("correlation_id")
    private String correlationId;

    /** 参数 schema 版本。 */
    @TableField("parameter_version")
    private String parameterVersion;

    /** 参数规范化快照的 SHA-256，不保存原始参数。 */
    @TableField("parameter_sha256")
    private String parameterSha256;

    /** 成功结果的脱敏摘要。 */
    @TableField("result_summary")
    private String resultSummary;

    /** 失败分类编码。 */
    @TableField("error_code")
    private String errorCode;

    /** 脱敏后的失败说明。 */
    @TableField("error_message")
    private String errorMessage;
}

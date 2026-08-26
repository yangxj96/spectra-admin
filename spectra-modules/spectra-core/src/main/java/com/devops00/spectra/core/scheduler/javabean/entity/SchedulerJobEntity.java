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

package com.devops00.spectra.core.scheduler.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.common.mybatis.PgJsonbTypeHandler;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerConcurrencyPolicy;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDefinitionStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDesiredState;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerMisfirePolicy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/** 统一调度任务定义。 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "scheduler_job", schema = "spectra_core", autoResultMap = true)
public class SchedulerJobEntity extends BaseEntity {

    @TableField(value = "job_key")
    private String jobKey;

    @TableField(value = "name")
    private String name;

    @TableField(value = "module")
    private String module;

    @TableField(value = "description")
    private String description;

    @TableField(value = "handler_key")
    private String handlerKey;

    @TableField(value = "job_type")
    private ScheduledJobType jobType;

    @TableField(value = "run_scope")
    private ScheduledRunScope runScope;

    @TableField(value = "definition_status")
    private SchedulerDefinitionStatus definitionStatus;

    @TableField(value = "desired_state")
    private SchedulerDesiredState desiredState;

    @TableField(value = "schedule_kind")
    private ScheduledScheduleKind scheduleKind;

    @TableField(value = "cron_expression")
    private String cronExpression;

    @TableField(value = "fixed_delay_ms")
    private Long fixedDelayMs;

    @TableField(value = "initial_delay_ms")
    private Long initialDelayMs;

    @TableField(value = "next_fire_at")
    private Instant nextFireAt;

    @TableField(value = "misfire_policy")
    private SchedulerMisfirePolicy misfirePolicy;

    @TableField(value = "concurrency_policy")
    private SchedulerConcurrencyPolicy concurrencyPolicy;

    @TableField(value = "execution_policy", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> executionPolicy;

    @TableField(value = "parameters", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> parameters;

    @TableField(value = "revision")
    private Long revision;
}

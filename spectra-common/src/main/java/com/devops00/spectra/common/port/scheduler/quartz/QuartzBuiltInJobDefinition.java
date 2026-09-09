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

package com.devops00.spectra.common.port.scheduler.quartz;

import org.quartz.Job;

import java.util.Objects;
import java.util.Optional;

/** Core 用于注册代码拥有型 Quartz 内置任务的不可变定义。 */
public record QuartzBuiltInJobDefinition(
                                         String typeKey,
                                         String displayName,
                                         Class<? extends Job> jobClass,
                                         String jobKey,
                                         QuartzParameterSchema parameterSchema,
                                         Optional<QuartzTriggerTemplate> defaultTrigger)
        implements
            QuartzJobDefinition {

    /**
     * 创建一个受保护的内置任务定义。
     *
     * @param typeKey         稳定的任务类型键
     * @param displayName     管理端展示名称
     * @param jobClass        受信任的 Quartz Job 类型
     * @param jobKey          固定的 Quartz JobKey 名称
     * @param parameterSchema 任务参数 Schema
     * @param defaultTrigger  默认 Trigger 模板
     */
    public QuartzBuiltInJobDefinition {
        Objects.requireNonNull(typeKey, "typeKey");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(jobClass, "jobClass");
        Objects.requireNonNull(jobKey, "jobKey");
        Objects.requireNonNull(parameterSchema, "parameterSchema");
        defaultTrigger = defaultTrigger == null ? Optional.empty() : defaultTrigger;
    }

    /**
     * 内置任务始终由代码保护，管理 API 不能将其当作普通任务处理。
     *
     * @return 始终为 true
     */
    @Override
    public boolean builtIn() {
        return true;
    }

    /**
     * 返回启动补注册和管理保护使用的固定 JobKey。
     *
     * @return 包含固定 JobKey 的 Optional
     */
    @Override
    public Optional<String> builtInJobKey() {
        return Optional.of(jobKey);
    }
}

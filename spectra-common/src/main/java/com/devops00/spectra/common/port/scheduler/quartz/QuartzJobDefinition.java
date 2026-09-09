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

import java.util.Optional;

/**
 * 可选业务模块向 Core 注册 Quartz Job 的公共契约。
 *
 * <p>契约只描述受信任的 Job 类型、参数规则和默认触发策略；Core 负责目录校验、资源注册和管理 API，
 * 业务模块不需要依赖 Core 的内部实现。</p>
 */
public interface QuartzJobDefinition {

    /**
     * 返回持久化和管理 API 使用的稳定任务类型键。
     *
     * @return 非空且在所有模块内唯一的任务类型键
     */
    String typeKey();

    /**
     * 返回管理端展示任务时使用的业务名称。
     *
     * @return 不为空的任务展示名称
     */
    String displayName();

    /**
     * 返回已经编译进应用并通过白名单校验的 Quartz Job 类。
     *
     * @return 受信任的 Job 实现类
     */
    Class<? extends Job> jobClass();

    /**
     * 返回任务是否由代码保护为内置任务。
     *
     * @return true 表示管理端不得删除或替换该任务定义
     */
    boolean builtIn();

    /**
     * 返回内置任务固定使用的 JobKey；普通任务不提供固定键。
     *
     * @return 内置任务键，普通任务返回空 Optional
     */
    Optional<String> builtInJobKey();

    /**
     * 返回 JobDataMap 可接受的版本化参数规则。
     *
     * @return 不可变参数 Schema
     */
    QuartzParameterSchema parameterSchema();

    /**
     * 返回任务首次注册时使用的默认 Trigger 策略。
     *
     * @return 默认 Trigger 模板；不需要自动调度时为空 Optional
     */
    Optional<QuartzTriggerTemplate> defaultTrigger();
}

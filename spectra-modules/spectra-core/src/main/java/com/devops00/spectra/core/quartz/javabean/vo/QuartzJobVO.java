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

package com.devops00.spectra.core.quartz.javabean.vo;

/**
 * 封装Quartz作业相关的响应数据。
 *
 * @param jobKey           Quartz 中作业的唯一 JobKey
 * @param group            Quartz Job 所属的调度器分组
 * @param displayName      Quartz Job 的展示名称
 * @param typeKey          Quartz 注册表中的作业类型键
 * @param protectedJob     该作业是否受内置定义保护
 * @param jobClassName     Quartz Job 实现类的全限定类名
 * @param parameterVersion 参数版本
 * @param parametersJson   参数JSON
 * @param trigger          与 Quartz Job 关联的唯一触发器
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record QuartzJobVO(String jobKey, String group, String displayName, String typeKey,
                          boolean protectedJob, String jobClassName, String parameterVersion,
                          String parametersJson, QuartzTriggerVO trigger) {
}

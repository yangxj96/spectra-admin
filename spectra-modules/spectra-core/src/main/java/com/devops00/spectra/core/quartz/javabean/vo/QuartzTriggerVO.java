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

import com.devops00.spectra.common.port.quartz.QuartzTriggerTemplate;

import java.time.LocalDateTime;

/**
 * 封装Quartz触发器相关的响应数据。
 *
 * @param triggerKey         触发器键
 * @param triggerType        触发器类型
 * @param state              当前对象所处的业务状态
 * @param cronExpression     Cron 表达式
 * @param intervalMs         固定间隔触发器的间隔时长（毫秒）
 * @param oneShot            是否仅触发一次
 * @param timeZone           使用的时区
 * @param misfireInstruction 错过触发
 * @param startAt            触发器开始生效的时间
 * @param previousFireAt     上一次实际触发时间
 * @param nextFireAt         下一次计划触发时间
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record QuartzTriggerVO(String triggerKey, QuartzTriggerTemplate.TriggerType triggerType,
                              String state, String cronExpression, Long intervalMs, boolean oneShot,
                              String timeZone, QuartzTriggerTemplate.MisfirePolicy misfireInstruction,
                              LocalDateTime startAt, LocalDateTime previousFireAt, LocalDateTime nextFireAt) {
}

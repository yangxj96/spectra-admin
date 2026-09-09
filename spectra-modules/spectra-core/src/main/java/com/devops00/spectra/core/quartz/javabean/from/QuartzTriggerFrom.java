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

package com.devops00.spectra.core.quartz.javabean.from;

import com.devops00.spectra.common.port.quartz.QuartzTriggerTemplate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/** Quartz Cron/Simple Trigger 的受限请求模型。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuartzTriggerFrom {

    /** 请求创建的 Quartz Trigger 类型。 */
    @NotNull(message = "Trigger 类型不能为空")
    private QuartzTriggerTemplate.TriggerType triggerType;

    /** Cron 表达式；仅 Cron Trigger 使用 Quartz 六字段或七字段格式。 */
    private String cronExpression;

    /** Cron 使用的 IANA 时区，例如 {@code Asia/Shanghai}。 */
    private String timeZone;

    /** Simple Trigger 的首次触发时间；未提供时从当前时间开始。 */
    private Instant startAt;

    /** 固定间隔毫秒数；周期 Simple Trigger 必须大于 0。 */
    @Positive(message = "Simple Trigger 间隔必须大于 0")
    private Long intervalMs;

    /** 是否为一次性 Simple Trigger；为 false 时使用 intervalMs 表示固定间隔。 */
    private boolean oneShot;

    /** 允许的 Quartz misfire 处理策略。 */
    private QuartzTriggerTemplate.MisfirePolicy misfireInstruction;
}

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

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 普通 Quartz Job 修改请求；JobKey、Job 类型和 Job 类不可由请求修改。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuartzJobUpdateFrom {

    /** 更新后的管理端展示名称。 */
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 120, message = "任务名称不能超过 120 个字符")
    private String displayName;

    /** 含 schema version 的 JSON 参数。 */
    @NotBlank(message = "任务参数不能为空")
    @Size(max = 20_000, message = "任务参数不能超过 20000 个字符")
    private String parametersJson;

    /** 更新后的唯一 Trigger 配置。 */
    @Valid
    @NotNull(message = "Trigger 不能为空")
    private QuartzTriggerFrom trigger;
}

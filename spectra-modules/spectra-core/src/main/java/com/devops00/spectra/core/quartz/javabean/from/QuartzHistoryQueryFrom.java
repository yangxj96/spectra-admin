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

import com.devops00.spectra.core.quartz.javabean.enums.QuartzExecutionHistoryStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Quartz 执行历史筛选条件。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuartzHistoryQueryFrom {

    /** 要筛选的 JobKey；为空表示不按 Job 筛选。 */
    private String jobKey;

    /** 要筛选的 TriggerKey；为空表示不按 Trigger 筛选。 */
    private String triggerKey;

    /** 要筛选的执行状态；为空表示查询全部状态。 */
    private QuartzExecutionHistoryStatus status;

    /** 起始时间，必须是带时区的 ISO-8601 时间；为空表示不限制起点。 */
    private String from;

    /** 结束时间，必须是带时区的 ISO-8601 时间；为空表示不限制终点。 */
    private String to;
}

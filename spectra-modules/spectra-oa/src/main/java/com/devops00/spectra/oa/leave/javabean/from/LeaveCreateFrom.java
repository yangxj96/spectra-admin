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

package com.devops00.spectra.oa.leave.javabean.from;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 请假申请保存参数。时间使用 ISO-8601 字符串。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Data
public class LeaveCreateFrom {

    /**
     * 请假类型编码。
     */
    @NotBlank(message = "请假类型不能为空")
    private String leaveTypeCode;

    /**
     * 开始时间。
     */
    @NotBlank(message = "开始时间不能为空")
    private String startTime;

    /**
     * 结束时间。
     */
    @NotBlank(message = "结束时间不能为空")
    private String endTime;

    /**
     * 原因。
     */
    @NotBlank(message = "请假事由不能为空")
    private String reason;

    /**
     * 联系地址。
     */
    private String contactAddress;

    /**
     * 是否自动计算时长。
     */
    @NotNull(message = "请假时长不能为空")
    private Boolean calculateDuration = true;
}

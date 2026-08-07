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

package com.devops00.spectra.oa.calendar.javabean.from;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/// 日程保存参数。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Data
public class CalendarSaveFrom {
    @NotBlank(message = "日程标题不能为空")
    private String title;

    private String content;
    private String startTime;
    private String endTime;
    private Boolean allDay = false;
    private String eventType = "PERSONAL";
    private String visibility = "PRIVATE";
    private String location;
    private String participantIds;
}

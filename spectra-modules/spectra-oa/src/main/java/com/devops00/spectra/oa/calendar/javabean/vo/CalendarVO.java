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

package com.devops00.spectra.oa.calendar.javabean.vo;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

/// 日程响应视图。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Data
public class CalendarVO {

    /// 主键 ID。
    private UUID id;

    /// 所有者 ID。
    private UUID ownerId;

    /// 标题。
    private String title;

    /// 内容。
    private String content;

    /// 开始时间。
    private LocalDateTime startTime;

    /// 结束时间。
    private LocalDateTime endTime;

    /// 是否全天。
    private Boolean allDay;

    /// 事件类型字段。
    private String eventType;

    /// 可见范围。
    private String visibility;

    /// 位置。
    private String location;

    /// 参与人 ID 列表。
    private String participantIds;

    /// 来源类型字段。
    private String sourceType;

    /// 来源 ID。
    private UUID sourceId;
}

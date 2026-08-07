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

package com.devops00.spectra.oa.calendar.javabean.entity;

import java.time.Instant;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_calendar", schema = "spectra_oa")
@DataScope(ignore = true)
/// OA 日程实体。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
public class Calendar extends BaseEntity {
    @TableField("department_id")
    private UUID departmentId;

    @TableField("owner_id")
    private UUID ownerId;

    @TableField("title")
    private String title;

    @TableField("content")
    private String content;

    @TableField("start_time")
    private Instant startTime;

    @TableField("end_time")
    private Instant endTime;

    @TableField("all_day")
    private Boolean allDay;

    @TableField("event_type")
    private String eventType;

    @TableField("visibility")
    private String visibility;

    @TableField("location")
    private String location;

    @TableField("participant_ids")
    private String participantIds;

    @TableField("source_type")
    private String sourceType;

    @TableField("source_id")
    private UUID sourceId;
}

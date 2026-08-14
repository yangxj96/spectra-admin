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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;

/**
 * OA 日程实体。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_calendar", schema = "spectra_oa")
@DataScope(readPermission = "oa:calendar:read", writePermission = "oa:calendar:update", ownerColumn = "owner_id")
public class Calendar extends BaseEntity {

    /**
     * 部门 ID。
     */
    @TableField("department_id")
    private UUID departmentId;

    /**
     * 所有者 ID。
     */
    @TableField("owner_id")
    private UUID ownerId;

    /**
     * 标题。
     */
    @TableField("title")
    private String title;

    /**
     * 内容。
     */
    @TableField("content")
    private String content;

    /**
     * 开始时间。
     */
    @TableField("start_time")
    private Instant startTime;

    /**
     * 结束时间。
     */
    @TableField("end_time")
    private Instant endTime;

    /**
     * 是否全天。
     */
    @TableField("all_day")
    private Boolean allDay;

    /**
     * 事件类型字段。
     */
    @TableField("event_type")
    private String eventType;

    /**
     * 可见范围。
     */
    @TableField("visibility")
    private String visibility;

    /**
     * 位置。
     */
    @TableField("location")
    private String location;

    /**
     * 参与人 ID 列表。
     */
    @TableField("participant_ids")
    private String participantIds;

    /**
     * 来源类型字段。
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 来源 ID。
     */
    @TableField("source_id")
    private UUID sourceId;
}

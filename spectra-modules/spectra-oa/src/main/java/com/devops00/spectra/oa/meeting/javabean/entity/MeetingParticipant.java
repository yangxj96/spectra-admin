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

package com.devops00.spectra.oa.meeting.javabean.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/// OA-会议-参会人员表
///
/// @author yangxj96
/// @version 1.0
/// @since 2026-03-30 14:54
@Getter
@Setter
@ToString
@TableName(value = "oa_meeting_participant")
public class MeetingParticipant extends BaseEntity {

    /// 会议ID
    @TableField("meeting_id")
    private String meetingId;

    /// 参会人ID
    @TableField(value = "user_id")
    private UUID userId;

    /// 角色
    ///
    /// |值|说明|
    /// |----|----|
    /// |host|主持人|
    /// |attendee|参会人|
    /// |optional|可选参会人|
    @TableField("role")
    private String role;

    /// 状态
    ///
    /// |值|说明|
    /// |----|----|
    /// |pending|未响应|
    /// |accepted|已接受|
    /// |declined|已拒绝|
    /// |checked_in|已签到|
    @TableField("status")
    private String status;

    /// 是否确认/签到
    @TableField("check_in_at")
    private String checkInAt;

}

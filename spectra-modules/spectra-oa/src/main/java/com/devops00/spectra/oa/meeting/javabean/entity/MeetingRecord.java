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
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/// OA-会议-会议纪要
///
/// @author yangxj96
/// @version 1.0
/// @since 2026-03-30 14:53
@Getter
@Setter
@ToString
@TableName(value = "oa_meeting_record", schema = "spectra_oa")
@DataScope(column = "department_id", relations = {
        @DataScope.Relation(
                schema = "spectra_oa",
                table = "oa_meeting_participant",
                joinColumn = "meeting_id",
                userColumn = "user_id",
                mainColumn = "meeting_id"
        )
})
public class MeetingRecord extends BaseEntity {

    /// 会议ID
    @TableField(value = "meeting_id")
    private UUID meetingId;

    /// 参会人ID
    @TableField("content")
    private String content;

    /// 所属部门ID
    @TableField("department_id")
    private UUID departmentId;
}

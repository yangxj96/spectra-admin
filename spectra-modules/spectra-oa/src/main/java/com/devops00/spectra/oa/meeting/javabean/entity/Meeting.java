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
import com.devops00.spectra.oa.meeting.javabean.constant.MeetingStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/// OA-会议表主表实体
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/5 23:53
@Getter
@Setter
@ToString
@TableName(value = "oa_meeting", schema = "spectra_oa")
@DataScope(relations = {
        @DataScope.Relation(
                schema = "spectra_oa",
                table = "oa_meeting_participant",
                joinColumn = "meeting_id",
                userColumn = "user_id"
        )
})
public class Meeting extends BaseEntity {

    /// 会议标题
    @TableField("title")
    private String title;

    /// 发起人
    @TableField("initiator_id")
    private String initiatorId;

    /// 开始时间
    @TableField("start_time")
    private String startTime;

    /// 结束时间
    @TableField("end_time")
    private String endTime;

    /// 会议地点
    @TableField("location")
    private String location;

    /// 会议内容/议题
    @TableField("content")
    private String content;

    /// 会议业务状态
    @TableField("status")
    private MeetingStatus status;

    /// 工作流审核实例ID
    @TableField("process_instance_id")
    private String processInstanceId;

    /// 工作流审核状态
    @TableField("approval_status")
    private MeetingStatus approvalStatus;

    /// 所属部门ID
    @TableField("department_id")
    private UUID departmentId;

}

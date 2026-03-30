package com.devops00.spectra.oa.meeting.javabean.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/// OA-会议-参会人员表
///
/// @author Jack Young
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
    @TableField("user_id")
    private String userId;

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

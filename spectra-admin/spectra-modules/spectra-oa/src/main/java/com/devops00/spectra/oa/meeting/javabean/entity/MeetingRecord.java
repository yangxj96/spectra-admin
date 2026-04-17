package com.devops00.spectra.oa.meeting.javabean.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/// OA-会议-会议纪要
///
/// @author Jack Young
/// @version 1.0
/// @since 2026-03-30 14:53
@Getter
@Setter
@ToString
@TableName(value = "oa_meeting_record")
public class MeetingRecord extends BaseEntity {

    /// 会议ID
    @TableField(value = "meeting_id")
    private UUID meetingId;

    /// 参会人ID
    @TableField("content")
    private String content;

}

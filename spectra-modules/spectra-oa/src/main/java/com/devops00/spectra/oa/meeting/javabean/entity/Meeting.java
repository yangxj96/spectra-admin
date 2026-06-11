package com.devops00.spectra.oa.meeting.javabean.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/// OA-会议表主表实体
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/5 23:53
@Getter
@Setter
@ToString
@TableName(value = "oa_meeting")
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
    private String status;

    /// 工作流审核实例ID
    @TableField("process_instance_id")
    private String processInstanceId;

    /// 工作流审核状态
    @TableField("approval_status")
    private String approvalStatus;

}

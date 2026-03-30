package com.devops00.spectra.oa.meeting.javabean.from;


import java.util.List;

/// 会议创建
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/30 15:24
public class MeetingCreateFrom {

    /// 会议标题
    private String title;

    /// 发起人
    private String initiatorId;

    /// 开始时间
    private String startTime;

    /// 结束时间
    private String endTime;

    /// 会议地点
    private String location;

    /// 会议内容/议题
    private String content;

    /// 参会人员列表
    private List<MeetingParticipantFrom> participants;
}

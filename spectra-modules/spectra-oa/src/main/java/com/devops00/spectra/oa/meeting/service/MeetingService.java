package com.devops00.spectra.oa.meeting.service;

import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.oa.meeting.javabean.entity.Meeting;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingCreateFrom;

/// 会仪表-服务
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/30 11:47
public interface MeetingService extends BaseService<Meeting> {

    /// 创建一个会议
    ///
    /// @param from 入参
    void create(MeetingCreateFrom from);
}

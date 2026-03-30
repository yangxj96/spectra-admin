package com.devops00.spectra.oa.meeting.service.impl;


import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.oa.meeting.javabean.entity.Meeting;
import com.devops00.spectra.oa.meeting.mapper.MeetingMapper;
import com.devops00.spectra.oa.meeting.service.MeetingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/// 会仪表-服务默认实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/30 11:47
@Slf4j
@Service
public class MeetingServiceImpl extends BaseServiceImpl<MeetingMapper, Meeting> implements MeetingService {
}

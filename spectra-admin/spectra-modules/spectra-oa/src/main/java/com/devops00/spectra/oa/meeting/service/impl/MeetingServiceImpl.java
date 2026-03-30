package com.devops00.spectra.oa.meeting.service.impl;


import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.oa.meeting.javabean.converter.MeetingConverter;
import com.devops00.spectra.oa.meeting.javabean.entity.Meeting;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingCreateFrom;
import com.devops00.spectra.oa.meeting.mapper.MeetingMapper;
import com.devops00.spectra.oa.meeting.service.MeetingService;
import com.devops00.spectra.workflow.service.ProcessInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/// 会仪表-服务默认实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/30 11:47
@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingServiceImpl extends BaseServiceImpl<MeetingMapper, Meeting> implements MeetingService {

    private final MeetingConverter meetingConverter;

    private final ProcessInstanceService processInstanceService;

    @Override
    public void create(MeetingCreateFrom from) {
        Meeting entity = meetingConverter.toEntity(from);
        this.save(entity);
        // 启动流程
        // TODO 流程定义KEY
        String processId = processInstanceService.start("", entity.getId());
        // 补充流程信息后更新
        entity.setProcessInstanceId(processId);
        this.updateById(entity);
    }

}

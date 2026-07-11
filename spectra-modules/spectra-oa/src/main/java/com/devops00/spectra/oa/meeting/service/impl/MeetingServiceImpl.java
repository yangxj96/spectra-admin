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

package com.devops00.spectra.oa.meeting.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.oa.meeting.javabean.converter.MeetingConverter;
import com.devops00.spectra.oa.meeting.javabean.entity.Meeting;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingCreateFrom;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingPageFrom;
import com.devops00.spectra.oa.meeting.javabean.vo.MeetingVO;
import com.devops00.spectra.oa.meeting.mapper.MeetingMapper;
import com.devops00.spectra.oa.meeting.service.MeetingService;
import com.devops00.spectra.workflow.service.ProcessInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/// 会仪表-服务默认实现
///
/// @author yangxj96
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
        String processId = processInstanceService.start("", String.valueOf(entity.getId()));
        // 补充流程信息后更新
        entity.setProcessInstanceId(processId);
        this.updateById(entity);
    }

    @Override
    public IPage<MeetingVO> page(PageFrom page, MeetingPageFrom params) {
        var wrapper = new LambdaQueryWrapper<Meeting>();
        if (StringUtils.hasText(params.getTitle())) {
            wrapper.like(Meeting::getTitle, params.getTitle());
        }
        if (StringUtils.hasText(params.getStatus())) {
            wrapper.eq(Meeting::getStatus, params.getStatus());
        }
        wrapper.orderByDesc(Meeting::getCreatedAt);
        var result = this.page(page.toPage(), wrapper);
        var voPage = new Page<MeetingVO>(
                result.getCurrent(), result.getSize(), result.getTotal()
        );
        voPage.setRecords(meetingConverter.toVOList(result.getRecords()));
        return voPage;
    }

}

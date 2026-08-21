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
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationGateway;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationRequest;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.meeting.javabean.converter.MeetingConverter;
import com.devops00.spectra.oa.meeting.javabean.entity.Meeting;
import com.devops00.spectra.oa.meeting.javabean.entity.MeetingParticipant;
import com.devops00.spectra.oa.meeting.javabean.entity.MeetingRecord;
import com.devops00.spectra.oa.meeting.javabean.constant.MeetingStatus;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingCreateFrom;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingPageFrom;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingRecordFrom;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingResponseFrom;
import com.devops00.spectra.oa.meeting.javabean.vo.MeetingVO;
import com.devops00.spectra.oa.meeting.mapper.MeetingMapper;
import com.devops00.spectra.oa.meeting.mapper.MeetingParticipantMapper;
import com.devops00.spectra.oa.meeting.mapper.MeetingRecordMapper;
import com.devops00.spectra.oa.meeting.service.MeetingService;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 会议业务服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Service
@RequiredArgsConstructor
public class MeetingServiceImpl extends BaseServiceImpl<MeetingMapper, Meeting> implements MeetingService {

    private final MeetingConverter meetingConverter;
    private final MeetingParticipantMapper participantMapper;
    private final MeetingRecordMapper recordMapper;
    private final NotificationGateway notificationGateway;
    private final UserService userService;
    private final TimeMapper timeMapper;
    private final SecurityContextAccessor securityContextAccessor;

    @Override
    @Transactional
    public void created(MeetingCreateFrom from) {
        var user = securityContextAccessor.currentUser();
        var userId = securityContextAccessor.currentUserId();
        if (user == null || userId == null || user.getDepartmentId() == null) {
            throw new DataSaveException("当前用户组织信息不可用");
        }
        var start = parse(from.getStartTime());
        var end = parse(from.getEndTime());
        if (!end.isAfter(start)) {
            throw new DataSaveException("会议结束时间必须晚于开始时间");
        }
        var entity = meetingConverter.toEntity(from);
        entity.setInitiatorId(userId.toString());
        entity.setDepartmentId(user.getDepartmentId());
        entity.setStatus(MeetingStatus.SCHEDULED);
        entity.setApprovalStatus(MeetingStatus.APPROVED);
        if (hasConflict(entity, start, end)) {
            throw new DataSaveException("同一地点存在时间重叠的会议");
        }
        if (!this.save(entity)) {
            throw new DataSaveException("保存会议失败");
        }
        var receivers = new java.util.ArrayList<UUID>();
        addParticipant(entity, userId, "host", user.getDepartmentId(), "accepted");
        receivers.add(userId);
        if (from.getParticipants() != null) {
            for (var fromParticipant : from.getParticipants()) {
                if (!StringUtils.hasText(fromParticipant.getUserId())) {
                    continue;
                }
                var participantId = UUID.fromString(fromParticipant.getUserId());
                if (participantId.equals(userId)) {
                    continue;
                }
                var participant = userService.getById(participantId);
                if (participant == null) {
                    continue;
                }
                addParticipant(entity, participantId, StringUtils.hasText(fromParticipant.getRole()) ? fromParticipant.getRole() : "attendee",
                        participant.getDepartmentId(), "pending");
                receivers.add(participantId);
            }
        }
        if (!receivers.isEmpty()) {
            notificationGateway.enqueue(NotificationRequest.inApp("oa:meeting:" + entity.getId(),
                    NotificationPurpose.OA_REMINDER, receivers.stream().distinct().toList(), "oa.meeting.invitation",
                    "会议邀请：" + entity.getTitle(), entity.getContent(), "OA_MEETING", entity.getId().toString(), "OA",
                    "/oa/meeting?id=" + entity.getId()));
        }
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
        wrapper.orderByAsc(Meeting::getStartTime);
        var result = this.page(page.toPage(), wrapper);
        var voPage = new Page<MeetingVO>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(meetingConverter.toVOList(result.getRecords()));
        return voPage;
    }

    @Override
    @Transactional
    public void respond(UUID meetingId, MeetingResponseFrom from) {
        var userId = securityContextAccessor.currentUserId();
        var participant = participantMapper.selectOne(new LambdaQueryWrapper<MeetingParticipant>()
                .eq(MeetingParticipant::getMeetingId, meetingId.toString())
                .eq(MeetingParticipant::getUserId, userId));
        if (participant == null) {
            throw new DataNotExistException("您不是该会议参与人");
        }
        if (!List.of("accepted", "declined", "pending").contains(from.getStatus())) {
            throw new DataSaveException("会议响应状态不正确");
        }
        participant.setStatus(from.getStatus());
        if (participantMapper.updateById(participant) != 1) {
            throw new DataSaveException("更新会议响应失败");
        }
    }

    @Override
    @Transactional
    public void checkIn(UUID meetingId) {
        var userId = securityContextAccessor.currentUserId();
        var participant = participantMapper.selectOne(new LambdaQueryWrapper<MeetingParticipant>()
                .eq(MeetingParticipant::getMeetingId, meetingId.toString())
                .eq(MeetingParticipant::getUserId, userId));
        if (participant == null) {
            throw new DataNotExistException("您不是该会议参与人");
        }
        participant.setStatus("checked_in");
        participant.setCheckInAt(Instant.now());
        participantMapper.updateById(participant);
    }

    @Override
    @Transactional
    public void saveRecord(UUID meetingId, MeetingRecordFrom from) {
        var meeting = this.getById(meetingId);
        var userId = securityContextAccessor.currentUserId();
        if (meeting == null || !userId.toString().equals(meeting.getInitiatorId())) {
            throw new DataNotExistException("只有会议发起人可以维护纪要");
        }
        var record = recordMapper.selectOne(new LambdaQueryWrapper<MeetingRecord>().eq(MeetingRecord::getMeetingId, meetingId));
        if (record == null) {
            record = new MeetingRecord();
            record.setMeetingId(meetingId);
            record.setDepartmentId(meeting.getDepartmentId());
        }
        record.setContent(from.getContent());
        if (record.getId() == null) {
            recordMapper.insert(record);
        } else {
            recordMapper.updateById(record);
        }
    }

    private void addParticipant(Meeting meeting, UUID userId, String role, UUID departmentId, String status) {
        var participant = new MeetingParticipant();
        participant.setMeetingId(meeting.getId().toString());
        participant.setUserId(userId);
        participant.setRole(role);
        participant.setStatus(status);
        participant.setDepartmentId(departmentId);
        participantMapper.insert(participant);
    }

    private boolean hasConflict(Meeting entity, Instant start, Instant end) {
        if (!StringUtils.hasText(entity.getLocation())) {
            return false;
        }
        var candidates = this.list(new LambdaQueryWrapper<Meeting>().eq(Meeting::getLocation, entity.getLocation())
                .ne(Meeting::getStatus,
                        MeetingStatus.CANCELLED));
        return candidates.stream()
                .anyMatch(item -> item.getStartTime() != null
                        && item.getEndTime() != null
                        && start.isBefore(item.getEndTime())
                        && end.isAfter(item.getStartTime()));
    }

    private Instant parse(String value) {
        if (!StringUtils.hasText(value)) {
            throw new DataSaveException("会议时间不能为空");
        }
        try {
            return timeMapper.toInstant(value);
        } catch (RuntimeException exception) {
            throw new DataSaveException("会议时间格式不正确");
        }
    }
}

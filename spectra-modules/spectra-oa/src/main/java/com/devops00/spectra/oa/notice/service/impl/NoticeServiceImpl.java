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

package com.devops00.spectra.oa.notice.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.core.notification.javabean.dto.NotificationBatchSendDTO;
import com.devops00.spectra.core.notification.service.NotificationService;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.oa.notice.javabean.converter.NoticeConverter;
import com.devops00.spectra.oa.notice.javabean.entity.Notice;
import com.devops00.spectra.oa.notice.javabean.entity.NoticeReader;
import com.devops00.spectra.oa.notice.javabean.from.NoticeCreateFrom;
import com.devops00.spectra.oa.notice.javabean.from.NoticePageFrom;
import com.devops00.spectra.oa.notice.javabean.vo.NoticeVO;
import com.devops00.spectra.oa.notice.mapper.NoticeMapper;
import com.devops00.spectra.oa.notice.mapper.NoticeReaderMapper;
import com.devops00.spectra.oa.notice.service.NoticeService;
import com.devops00.spectra.security.base.holder.SecUtil;

import lombok.RequiredArgsConstructor;

/**
 * 公告业务服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends BaseServiceImpl<NoticeMapper, Notice> implements NoticeService {

    private final NoticeReaderMapper noticeReaderMapper;
    private final NotificationService notificationService;
    private final UserService userService;
    private final NoticeConverter noticeConverter;
    private final TimeMapper timeMapper;

    @Override
    @Transactional
    public IPage<NoticeVO> page(PageFrom page, NoticePageFrom params) {
        var currentUser = SecUtil.getCurrentUser();
        var userId = SecUtil.getCurrentUserId();
        if (currentUser == null || userId == null) {
            return new Page<>(page.getPageNum(), page.getPageSize());
        }
        var wrapper = new LambdaQueryWrapper<Notice>()
                .and(q -> q.eq(Notice::getPublisherId, userId)
                        .or()
                        .eq(Notice::getStatus, "PUBLISHED")
                        .or(w -> w.eq(Notice::getStatus, "SCHEDULED").le(Notice::getPublishAt, Instant.now())))
                .and(q -> q.eq(Notice::getTargetType, "ALL")
                        .or(w -> w.eq(Notice::getTargetType, "DEPARTMENT").eq(Notice::getTargetDepartmentId, currentUser.getDepartmentId())));
        if (StringUtils.hasText(params.getKeyword())) {
            wrapper.and(q -> q.like(Notice::getTitle, params.getKeyword()).or().like(Notice::getSummary, params.getKeyword()));
        }
        if (StringUtils.hasText(params.getStatus())) {
            wrapper.eq(Notice::getStatus, params.getStatus());
        }
        wrapper.orderByDesc(Notice::getPublishAt).orderByDesc(Notice::getCreatedAt);
        var result = this.page(page.toPage(), wrapper);
        activateDueNotices(result.getRecords());
        var voPage = new Page<NoticeVO>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::assembleView).toList());
        return voPage;
    }

    @Override
    public NoticeVO get(UUID id) {
        var notice = require(id);
        var userId = SecUtil.getCurrentUserId();
        if (userId == null || !isVisible(notice, userId)) {
            throw new DataNotExistException("公告不存在或无权访问");
        }
        return assembleView(notice);
    }

    @Override
    @Transactional
    public Notice createDraft(NoticeCreateFrom from) {
        var user = SecUtil.getCurrentUser();
        var userId = SecUtil.getCurrentUserId();
        if (user == null || userId == null) {
            throw new DataSaveException("当前用户上下文不可用");
        }
        var targetType = StringUtils.hasText(from.getTargetType()) ? from.getTargetType().toUpperCase() : "ALL";
        if ("DEPARTMENT".equals(targetType) && from.getTargetDepartmentId() == null) {
            throw new DataSaveException("部门公告必须指定目标部门");
        }
        var notice = noticeConverter.toEntity(from);
        notice.setStatus("DRAFT");
        notice.setTargetType(targetType);
        notice.setDepartmentId(user.getDepartmentId());
        notice.setPublisherId(userId);
        notice.setRequiredRead(Boolean.TRUE.equals(notice.getRequiredRead()));
        notice.setPublishAt(parseTime(from.getPublishAt()));
        if (!this.save(notice)) {
            throw new DataSaveException("保存公告草稿失败");
        }
        return notice;
    }

    @Override
    @Transactional
    public void publish(UUID id) {
        var notice = require(id);
        ensurePublisher(notice);
        if (!"DRAFT".equals(notice.getStatus()) && !"SCHEDULED".equals(notice.getStatus())) {
            throw new DataSaveException("当前公告状态不允许发布");
        }
        var publishAt = notice.getPublishAt() == null ? Instant.now() : notice.getPublishAt();
        notice.setStatus(publishAt.isAfter(Instant.now()) ? "SCHEDULED" : "PUBLISHED");
        notice.setPublishAt(publishAt);
        if (!this.updateById(notice)) {
            throw new DataSaveException("发布公告失败");
        }
        if ("PUBLISHED".equals(notice.getStatus())) {
            sendNotifications(notice);
        }
    }

    @Override
    @Transactional
    public void revoke(UUID id) {
        var notice = require(id);
        ensurePublisher(notice);
        if (!"PUBLISHED".equals(notice.getStatus()) && !"SCHEDULED".equals(notice.getStatus())) {
            throw new DataSaveException("当前公告状态不允许撤回");
        }
        notice.setStatus("REVOKED");
        if (!this.updateById(notice)) {
            throw new DataSaveException("撤回公告失败");
        }
    }

    @Override
    @Transactional
    public void markRead(UUID id) {
        var userId = SecUtil.getCurrentUserId();
        var notice = require(id);
        if (userId == null || !isVisible(notice, userId)) {
            throw new DataNotExistException("公告不存在或无权访问");
        }
        var reader = noticeReaderMapper
                .selectOne(new LambdaQueryWrapper<NoticeReader>().eq(NoticeReader::getNoticeId, id).eq(NoticeReader::getUserId, userId));
        if (reader == null) {
            reader = new NoticeReader();
            reader.setNoticeId(id);
            reader.setUserId(userId);
            reader.setReadAt(Instant.now());
            noticeReaderMapper.insert(reader);
        } else if (reader.getReadAt() == null) {
            reader.setReadAt(Instant.now());
            noticeReaderMapper.updateById(reader);
        }
    }

    private NoticeVO assembleView(Notice notice) {
        var userId = SecUtil.getCurrentUserId();
        if (userId == null) {
            return noticeConverter.toVO(notice);
        }
        var reader = noticeReaderMapper
                .selectOne(new LambdaQueryWrapper<NoticeReader>().eq(NoticeReader::getNoticeId, notice.getId()).eq(NoticeReader::getUserId, userId));
        return noticeConverter.toVO(notice, reader);
    }

    private boolean isVisible(Notice notice, UUID userId) {
        if (userId.equals(notice.getPublisherId())) {
            return true;
        }
        if (!"PUBLISHED".equals(notice.getStatus()) && !"SCHEDULED".equals(notice.getStatus())) {
            return false;
        }
        if (notice.getPublishAt() != null && notice.getPublishAt().isAfter(Instant.now())) {
            return false;
        }
        if ("ALL".equals(notice.getTargetType())) {
            return true;
        }
        var user = userService.getById(userId);
        return user != null && notice.getTargetDepartmentId() != null && notice.getTargetDepartmentId().equals(user.getDepartmentId());
    }

    private void ensurePublisher(Notice notice) {
        if (!Objects.equals(notice.getPublisherId(), SecUtil.getCurrentUserId())) {
            throw new DataNotExistException("公告不存在或无权操作");
        }
    }

    private void sendNotifications(Notice notice) {
        var wrapper = new LambdaQueryWrapper<User>();
        if ("DEPARTMENT".equals(notice.getTargetType())) {
            wrapper.eq(User::getDepartmentId, notice.getTargetDepartmentId());
        }
        List<UUID> receiverIds = userService.list(wrapper).stream().map(User::getId).toList();
        if (receiverIds.isEmpty()) {
            return;
        }
        var dto = new NotificationBatchSendDTO();
        dto.setTitle(notice.getTitle());
        dto.setContent(notice.getSummary() == null ? notice.getContent() : notice.getSummary());
        dto.setType("oa_notice");
        dto.setSenderId(notice.getPublisherId());
        dto.setLink("/oa/notice?id=" + notice.getId());
        dto.setReceiverIds(receiverIds);
        notificationService.batchSend(dto);
    }

    private void activateDueNotices(List<Notice> notices) {
        var now = Instant.now();
        notices.stream()
                .filter(notice -> "SCHEDULED".equals(notice.getStatus()) && notice.getPublishAt() != null && !notice.getPublishAt().isAfter(now))
                .forEach(notice -> {
                    notice.setStatus("PUBLISHED");
                    if (this.updateById(notice)) {
                        sendNotifications(notice);
                    }
                });
    }

    private Notice require(UUID id) {
        var notice = this.getById(id);
        if (notice == null) {
            throw new DataNotExistException("公告不存在");
        }
        return notice;
    }

    private Instant parseTime(String value) {
        if (!StringUtils.hasText(value)) {
            return Instant.now();
        }
        try {
            return timeMapper.toInstant(value);
        } catch (RuntimeException exception) {
            throw new DataSaveException("公告发布时间格式不正确");
        }
    }
}

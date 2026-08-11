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

package com.devops00.spectra.notification.inbox.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.notification.inbox.javabean.converter.NotificationInboxConverter;
import com.devops00.spectra.notification.inbox.javabean.entity.NotificationInboxEntity;
import com.devops00.spectra.notification.inbox.javabean.from.NotificationQueryFrom;
import com.devops00.spectra.notification.inbox.javabean.vo.NotificationInboxVO;
import com.devops00.spectra.notification.inbox.mapper.NotificationInboxMapper;
import com.devops00.spectra.notification.inbox.service.NotificationInboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 当前用户消息中心服务实现。 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationInboxServiceImpl implements NotificationInboxService {

    private final NotificationInboxMapper mapper;
    private final NotificationInboxConverter converter;
    private final TimeMapper timeMapper;

    @Override
    public IPage<NotificationInboxVO> page(PageFrom page, UUID tenantId, UUID userId, NotificationQueryFrom params) {
        var query = new LambdaQueryWrapper<NotificationInboxEntity>().eq(NotificationInboxEntity::getTenantId, tenantId)
                .eq(NotificationInboxEntity::getRecipientUserId, userId)
                .isNull(NotificationInboxEntity::getDeleted)
                .isNull(NotificationInboxEntity::getArchivedAt)
                .orderByDesc(NotificationInboxEntity::getCreatedAt);
        if (params != null) {
            applyType(query, params.getType());
            if (params.getIsRead() != null) {
                if (params.getIsRead()) {
                    query.isNotNull(NotificationInboxEntity::getReadAt);
                } else {
                    query.isNull(NotificationInboxEntity::getReadAt);
                }
            }
            if (StringUtils.hasText(params.getKeyword())) {
                query.and(item -> item.like(NotificationInboxEntity::getTitle, params.getKeyword())
                        .or()
                        .like(NotificationInboxEntity::getContent, params.getKeyword()));
            }
            if (StringUtils.hasText(params.getStartTime())) {
                query.ge(NotificationInboxEntity::getCreatedAt, parseTime(params.getStartTime()));
            }
            if (StringUtils.hasText(params.getEndTime())) {
                query.le(NotificationInboxEntity::getCreatedAt, parseTime(params.getEndTime()));
            }
        }
        return converter.toVOPage(mapper.selectPage(page.toPage(), query));
    }

    @Override
    public long unreadCount(UUID tenantId, UUID userId) {
        return mapper.selectCount(new LambdaQueryWrapper<NotificationInboxEntity>().eq(NotificationInboxEntity::getTenantId, tenantId)
                .eq(NotificationInboxEntity::getRecipientUserId, userId)
                .isNull(NotificationInboxEntity::getReadAt)
                .isNull(NotificationInboxEntity::getDeleted)
                .isNull(NotificationInboxEntity::getArchivedAt));
    }

    @Override
    public NotificationInboxVO detail(UUID id, UUID tenantId, UUID userId) {
        return converter.toVO(owned(id, tenantId, userId));
    }

    @Override
    @Transactional
    public void markAsRead(UUID id, UUID tenantId, UUID userId) {
        var updated = mapper.update(null, new LambdaUpdateWrapper<NotificationInboxEntity>()
                .eq(NotificationInboxEntity::getId, id)
                .eq(NotificationInboxEntity::getTenantId, tenantId)
                .eq(NotificationInboxEntity::getRecipientUserId, userId)
                .isNull(NotificationInboxEntity::getDeleted)
                .isNull(NotificationInboxEntity::getArchivedAt)
                .isNull(NotificationInboxEntity::getReadAt)
                .set(NotificationInboxEntity::getReadAt, Instant.now()));
        if (updated == 0
            && mapper.selectCount(new LambdaQueryWrapper<NotificationInboxEntity>()
                    .eq(NotificationInboxEntity::getId, id)
                    .eq(NotificationInboxEntity::getTenantId, tenantId)
                    .eq(NotificationInboxEntity::getRecipientUserId, userId)
                    .isNull(NotificationInboxEntity::getDeleted)) == 0) {
            throw new DataNotExistException("消息不存在");
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID tenantId, UUID userId) {
        mapper.update(null, new LambdaUpdateWrapper<NotificationInboxEntity>().eq(NotificationInboxEntity::getTenantId, tenantId)
                .eq(NotificationInboxEntity::getRecipientUserId, userId)
                .isNull(NotificationInboxEntity::getDeleted)
                .isNull(NotificationInboxEntity::getArchivedAt)
                .isNull(NotificationInboxEntity::getReadAt)
                .set(NotificationInboxEntity::getReadAt, Instant.now()));
    }

    @Override
    @Transactional
    public void deleteById(UUID id, UUID tenantId, UUID userId) {
        var updated = mapper.update(null, new LambdaUpdateWrapper<NotificationInboxEntity>().eq(NotificationInboxEntity::getId, id)
                .eq(NotificationInboxEntity::getTenantId, tenantId)
                .eq(NotificationInboxEntity::getRecipientUserId, userId)
                .isNull(NotificationInboxEntity::getDeleted)
                .set(NotificationInboxEntity::getDeleted, Instant.now()));
        if (updated == 0) {
            throw new DataNotExistException("消息不存在");
        }
    }

    @Override
    @Transactional
    public void batchDelete(List<UUID> ids, UUID tenantId, UUID userId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        mapper.update(null, new LambdaUpdateWrapper<NotificationInboxEntity>().in(NotificationInboxEntity::getId, ids)
                .eq(NotificationInboxEntity::getTenantId, tenantId)
                .eq(NotificationInboxEntity::getRecipientUserId, userId)
                .isNull(NotificationInboxEntity::getDeleted)
                .set(NotificationInboxEntity::getDeleted, Instant.now()));
    }

    private NotificationInboxEntity owned(UUID id, UUID tenantId, UUID userId) {
        var entity = mapper.selectOne(new LambdaQueryWrapper<NotificationInboxEntity>().eq(NotificationInboxEntity::getId, id)
                .eq(NotificationInboxEntity::getTenantId, tenantId)
                .eq(NotificationInboxEntity::getRecipientUserId, userId)
                .isNull(NotificationInboxEntity::getDeleted));
        if (entity == null) {
            throw new DataNotExistException("消息不存在");
        }
        return entity;
    }

    private void applyType(LambdaQueryWrapper<NotificationInboxEntity> query, String type) {
        if (!StringUtils.hasText(type) || "all".equalsIgnoreCase(type)) {
            return;
        }
        var normalized = type.trim().toLowerCase();
        switch (normalized) {
            case "system" -> query.eq(NotificationInboxEntity::getPurpose, "SYSTEM_NOTICE");
            case "workflow" -> query.in(NotificationInboxEntity::getPurpose, "WORKFLOW_TODO", "WORKFLOW_RESULT");
            case "oa" -> query.in(NotificationInboxEntity::getPurpose, "OA_NOTICE", "OA_REMINDER");
            case "inner_mail" -> query.eq(NotificationInboxEntity::getPurpose, "INNER_MESSAGE");
            case "approval" -> query.eq(NotificationInboxEntity::getPurpose, "WORKFLOW_TODO");
            default -> query.eq(NotificationInboxEntity::getPurpose, normalized.toUpperCase());
        }
    }

    private Instant parseTime(String value) {
        try {
            return timeMapper.toInstant(value);
        } catch (RuntimeException exception) {
            throw new DataSaveException("消息时间格式不正确");
        }
    }
}

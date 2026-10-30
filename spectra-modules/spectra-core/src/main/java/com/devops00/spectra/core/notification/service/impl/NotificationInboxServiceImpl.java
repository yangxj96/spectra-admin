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

package com.devops00.spectra.core.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.core.notification.javabean.entity.NotificationMessageEntity;
import com.devops00.spectra.core.notification.javabean.from.NotificationQueryFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationInboxVO;
import com.devops00.spectra.core.notification.mapper.NotificationMessageMapper;
import com.devops00.spectra.core.notification.service.NotificationInboxService;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 当前用户消息中心服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Service
@RequiredArgsConstructor
public class NotificationInboxServiceImpl implements NotificationInboxService {

    private final NotificationMessageMapper mapper;
    private final TimeMapper timeMapper;

    @Override
    public IPage<NotificationInboxVO> page(PageFrom page, UUID userId, NotificationQueryFrom query) {
        var queryPage = page.<NotificationMessageEntity>toPage(Map.of("id", "id", "created_at", "created_at"));
        var wrapper = ownedQuery(userId).orderByDesc(NotificationMessageEntity::getCreatedAt);
        if (query != null) {
            if (query.getIsRead() != null) {
                if (query.getIsRead()) {
                    wrapper.isNotNull(NotificationMessageEntity::getReadAt);
                } else {
                    wrapper.isNull(NotificationMessageEntity::getReadAt);
                }
            }
            if (StringUtils.hasText(query.getKeyword())) {
                wrapper.and(item -> item.like(NotificationMessageEntity::getTitle, query.getKeyword())
                        .or()
                        .like(NotificationMessageEntity::getContent, query.getKeyword()));
            }
            if (StringUtils.hasText(query.getStartTime())) {
                wrapper.ge(NotificationMessageEntity::getCreatedAt, timeMapper.toInstant(query.getStartTime()));
            }
            if (StringUtils.hasText(query.getEndTime())) {
                wrapper.le(NotificationMessageEntity::getCreatedAt, timeMapper.toInstant(query.getEndTime()));
            }
        }
        var result = mapper.selectPage(queryPage, wrapper);
        var voPage = new Page<NotificationInboxVO>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public long unreadCount(UUID userId) {
        return mapper.selectCount(ownedQuery(userId).isNull(NotificationMessageEntity::getReadAt));
    }

    @Override
    public NotificationInboxVO detail(UUID id, UUID userId) {
        return toVO(owned(id, userId));
    }

    @Override
    @Transactional
    public void markAsRead(UUID id, UUID userId) {
        var updated = mapper.update(null, new LambdaUpdateWrapper<NotificationMessageEntity>()
                .eq(NotificationMessageEntity::getId, id)
                .eq(NotificationMessageEntity::getRecipientUserId, userId)
                .eq(NotificationMessageEntity::getChannel, NotificationChannel.IN_APP.name())
                .isNull(NotificationMessageEntity::getDeleted)
                .isNull(NotificationMessageEntity::getReadAt)
                .set(NotificationMessageEntity::getReadAt, Instant.now()));
        if (updated == 0) {
            owned(id, userId);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        mapper.update(null, new LambdaUpdateWrapper<NotificationMessageEntity>()
                .eq(NotificationMessageEntity::getRecipientUserId, userId)
                .eq(NotificationMessageEntity::getChannel, NotificationChannel.IN_APP.name())
                .isNull(NotificationMessageEntity::getDeleted)
                .isNull(NotificationMessageEntity::getReadAt)
                .set(NotificationMessageEntity::getReadAt, Instant.now()));
    }

    @Override
    @Transactional
    public void deleteById(UUID id, UUID userId) {
        var updated = mapper.update(null, new LambdaUpdateWrapper<NotificationMessageEntity>()
                .eq(NotificationMessageEntity::getId, id)
                .eq(NotificationMessageEntity::getRecipientUserId, userId)
                .eq(NotificationMessageEntity::getChannel, NotificationChannel.IN_APP.name())
                .isNull(NotificationMessageEntity::getDeleted)
                .set(NotificationMessageEntity::getDeleted, Instant.now()));
        if (updated == 0) {
            throw new DataNotExistException("消息不存在");
        }
    }

    @Override
    @Transactional
    public void batchDelete(List<UUID> ids, UUID userId) {
        var targetIds = ids == null ? List.<UUID>of() : ids.stream().filter(Objects::nonNull).distinct().toList();
        if (targetIds.isEmpty()) {
            return;
        }
        mapper.update(null, new LambdaUpdateWrapper<NotificationMessageEntity>()
                .in(NotificationMessageEntity::getId, targetIds)
                .eq(NotificationMessageEntity::getRecipientUserId, userId)
                .eq(NotificationMessageEntity::getChannel, NotificationChannel.IN_APP.name())
                .isNull(NotificationMessageEntity::getDeleted)
                .set(NotificationMessageEntity::getDeleted, Instant.now()));
    }

    /**
     * 构造当前用户站内信查询条件。
     *
     * @param userId 当前用户 ID
     * @return 查询条件
     */
    private LambdaQueryWrapper<NotificationMessageEntity> ownedQuery(UUID userId) {
        return new LambdaQueryWrapper<NotificationMessageEntity>()
                .eq(NotificationMessageEntity::getRecipientUserId, userId)
                .eq(NotificationMessageEntity::getChannel, NotificationChannel.IN_APP.name())
                .isNull(NotificationMessageEntity::getDeleted);
    }

    /**
     * 查询当前用户拥有的消息。
     *
     * @param id     消息 ID
     * @param userId 当前用户 ID
     * @return 消息实体
     */
    private NotificationMessageEntity owned(UUID id, UUID userId) {
        var entity = mapper.selectOne(ownedQuery(userId).eq(NotificationMessageEntity::getId, id));
        if (entity == null) {
            throw new DataNotExistException("消息不存在");
        }
        return entity;
    }

    /**
     * 转换为响应对象。
     *
     * @param entity 消息实体
     * @return 响应对象
     */
    private NotificationInboxVO toVO(NotificationMessageEntity entity) {
        var vo = new NotificationInboxVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setContent(entity.getContent());
        vo.setTemplateCode(entity.getTemplateCode());
        vo.setLink(entity.getLink());
        vo.setIsRead(entity.getReadAt() != null);
        vo.setReadAt(timeMapper.toOffsetDateTime(entity.getReadAt()));
        vo.setReceiverUserId(entity.getRecipientUserId());
        vo.setCreatedAt(timeMapper.toOffsetDateTime(entity.getCreatedAt()));
        return vo;
    }
}

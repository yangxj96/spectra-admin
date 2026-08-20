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

package com.devops00.spectra.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.notification.javabean.converter.NotificationInboxConverter;
import com.devops00.spectra.notification.javabean.entity.NotificationInboxEntity;
import com.devops00.spectra.notification.javabean.from.NotificationQueryFrom;
import com.devops00.spectra.notification.javabean.vo.NotificationInboxVO;
import com.devops00.spectra.notification.mapper.NotificationInboxMapper;
import com.devops00.spectra.notification.service.NotificationInboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 当前用户消息中心服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationInboxServiceImpl implements NotificationInboxService {

    /**
     * 消息收件箱 Mapper。
     */
    private final NotificationInboxMapper mapper;
    /**
     * 实体与响应对象转换器。
     */
    private final NotificationInboxConverter converter;
    /**
     * 时间格式转换器。
     */
    private final TimeMapper timeMapper;

    /**
     * 分页查询指定用户仍可见的消息。
     */
    @Override
    public IPage<NotificationInboxVO> page(PageFrom page, UUID userId, NotificationQueryFrom params) {
        var query = new LambdaQueryWrapper<NotificationInboxEntity>().eq(NotificationInboxEntity::getReceiverUserId, userId)
                .isNull(NotificationInboxEntity::getDeleted)
                .orderByDesc(NotificationInboxEntity::getCreatedAt);
        if (params != null) {
            applyPurpose(query, params.getPurpose());
            if (params.getIsRead() != null) {
                if (params.getIsRead()) {
                    query.eq(NotificationInboxEntity::getIsRead, true);
                } else {
                    query.eq(NotificationInboxEntity::getIsRead, false);
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

    /**
     * 统计指定用户的未读消息数量。
     */
    @Override
    public long unreadCount(UUID userId) {
        return mapper.selectCount(new LambdaQueryWrapper<NotificationInboxEntity>().eq(NotificationInboxEntity::getReceiverUserId, userId)
                .eq(NotificationInboxEntity::getIsRead, false)
                .isNull(NotificationInboxEntity::getDeleted));
    }

    /**
     * 查询指定用户拥有的消息详情。
     */
    @Override
    public NotificationInboxVO detail(UUID id, UUID userId) {
        return converter.toVO(owned(id, userId));
    }

    /**
     * 将指定用户的一条消息标记为已读。
     */
    @Override
    @Transactional
    public void markAsRead(UUID id, UUID userId) {
        var updated = mapper.update(null, new LambdaUpdateWrapper<NotificationInboxEntity>()
                .eq(NotificationInboxEntity::getId, id)
                .eq(NotificationInboxEntity::getReceiverUserId, userId)
                .isNull(NotificationInboxEntity::getDeleted)
                .eq(NotificationInboxEntity::getIsRead, false)
                .set(NotificationInboxEntity::getIsRead, true)
                .set(NotificationInboxEntity::getReadAt, Instant.now()));
        if (updated == 0
                && mapper.selectCount(new LambdaQueryWrapper<NotificationInboxEntity>()
                        .eq(NotificationInboxEntity::getId, id)
                        .eq(NotificationInboxEntity::getReceiverUserId, userId)
                        .isNull(NotificationInboxEntity::getDeleted)) == 0) {
            throw new DataNotExistException("消息不存在");
        }
    }

    /**
     * 将指定用户的全部可见消息标记为已读。
     */
    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        mapper.update(null, new LambdaUpdateWrapper<NotificationInboxEntity>().eq(NotificationInboxEntity::getReceiverUserId, userId)
                .isNull(NotificationInboxEntity::getDeleted)
                .eq(NotificationInboxEntity::getIsRead, false)
                .set(NotificationInboxEntity::getIsRead, true)
                .set(NotificationInboxEntity::getReadAt, Instant.now()));
    }

    /**
     * 软删除指定用户拥有的一条消息。
     */
    @Override
    @Transactional
    public void deleteById(UUID id, UUID userId) {
        var updated = mapper.update(null, new LambdaUpdateWrapper<NotificationInboxEntity>().eq(NotificationInboxEntity::getId, id)
                .eq(NotificationInboxEntity::getReceiverUserId, userId)
                .isNull(NotificationInboxEntity::getDeleted)
                .set(NotificationInboxEntity::getDeleted, Instant.now()));
        if (updated == 0) {
            throw new DataNotExistException("消息不存在");
        }
    }

    /**
     * 批量软删除指定用户拥有的消息。
     */
    @Override
    @Transactional
    public void batchDelete(List<UUID> ids, UUID userId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        mapper.update(null, new LambdaUpdateWrapper<NotificationInboxEntity>().in(NotificationInboxEntity::getId, ids)
                .eq(NotificationInboxEntity::getReceiverUserId, userId)
                .isNull(NotificationInboxEntity::getDeleted)
                .set(NotificationInboxEntity::getDeleted, Instant.now()));
    }

    /**
     * 按收件人约束查询消息，避免越权读取。
     */
    private NotificationInboxEntity owned(UUID id, UUID userId) {
        var entity = mapper.selectOne(new LambdaQueryWrapper<NotificationInboxEntity>().eq(NotificationInboxEntity::getId, id)
                .eq(NotificationInboxEntity::getReceiverUserId, userId)
                .isNull(NotificationInboxEntity::getDeleted));
        if (entity == null) {
            throw new DataNotExistException("消息不存在");
        }
        return entity;
    }

    /**
     * 按通知用途筛选消息。
     */
    private void applyPurpose(LambdaQueryWrapper<NotificationInboxEntity> query, String purpose) {
        if (!StringUtils.hasText(purpose) || "all".equalsIgnoreCase(purpose)) {
            return;
        }
        query.eq(NotificationInboxEntity::getPurpose, purpose.trim().toUpperCase());
    }

    /**
     * 解析查询时间，统一转换为无时区时间点。
     */
    private Instant parseTime(String value) {
        try {
            return timeMapper.toInstant(value);
        } catch (RuntimeException exception) {
            throw new DataSaveException("消息时间格式不正确");
        }
    }
}

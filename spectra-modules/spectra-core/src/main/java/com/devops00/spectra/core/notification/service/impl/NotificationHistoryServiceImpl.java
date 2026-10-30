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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.notification.javabean.entity.NotificationMessageEntity;
import com.devops00.spectra.core.notification.javabean.from.NotificationHistoryQueryFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationMessageVO;
import com.devops00.spectra.core.notification.mapper.NotificationMessageMapper;
import com.devops00.spectra.core.notification.service.NotificationHistoryService;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.UUID;

/**
 * 通知发送历史查询服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Service
@RequiredArgsConstructor
public class NotificationHistoryServiceImpl implements NotificationHistoryService {

    private final NotificationMessageMapper mapper;
    private final TimeMapper timeMapper;

    @Override
    public IPage<NotificationMessageVO> page(PageFrom page, NotificationHistoryQueryFrom query) {
        var queryPage = page.<NotificationMessageEntity>toPage(Map.of(
                "id", "id",
                "created_at", "created_at",
                "status", "status"));
        var wrapper = new LambdaQueryWrapper<NotificationMessageEntity>()
                .isNull(NotificationMessageEntity::getDeleted);
        if (query != null) {
            if (query.getChannel() != null) {
                wrapper.eq(NotificationMessageEntity::getChannel, query.getChannel().name());
            }
            if (query.getStatus() != null) {
                wrapper.eq(NotificationMessageEntity::getStatus, query.getStatus().name());
            }
            if (StringUtils.hasText(query.getKeyword())) {
                wrapper.and(item -> item.like(NotificationMessageEntity::getTemplateCode, query.getKeyword())
                        .or()
                        .like(NotificationMessageEntity::getTitle, query.getKeyword())
                        .or()
                        .like(NotificationMessageEntity::getRecipientAddress, query.getKeyword()));
            }
            if (StringUtils.hasText(query.getStartTime())) {
                wrapper.ge(NotificationMessageEntity::getCreatedAt, timeMapper.toInstant(query.getStartTime()));
            }
            if (StringUtils.hasText(query.getEndTime())) {
                wrapper.le(NotificationMessageEntity::getCreatedAt, timeMapper.toInstant(query.getEndTime()));
            }
        }
        wrapper.orderByDesc(NotificationMessageEntity::getCreatedAt);
        var result = mapper.selectPage(queryPage, wrapper);
        var voPage = new Page<NotificationMessageVO>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    @Override
    public NotificationMessageVO detail(UUID id) {
        var entity = mapper.selectOne(new LambdaQueryWrapper<NotificationMessageEntity>()
                .eq(NotificationMessageEntity::getId, id)
                .isNull(NotificationMessageEntity::getDeleted));
        if (entity == null) {
            throw new DataNotExistException("通知发送记录不存在");
        }
        return toVO(entity);
    }

    /**
     * 转换为响应对象。
     *
     * @param entity 历史实体
     * @return 响应对象
     */
    private NotificationMessageVO toVO(NotificationMessageEntity entity) {
        var vo = new NotificationMessageVO();
        vo.setId(entity.getId());
        vo.setChannel(entity.getChannel());
        vo.setTemplateCode(entity.getTemplateCode());
        vo.setTitle(entity.getTitle());
        vo.setContent(entity.getContent());
        vo.setHtmlContent(entity.getHtmlContent());
        vo.setStatus(entity.getStatus());
        vo.setErrorCode(entity.getErrorCode());
        vo.setErrorMessage(entity.getErrorMessage());
        vo.setProvider(entity.getProvider());
        vo.setRecipientUserId(entity.getRecipientUserId());
        vo.setRecipientAddress(entity.getRecipientAddress());
        vo.setReadAt(timeMapper.toOffsetDateTime(entity.getReadAt()));
        vo.setBusinessType(entity.getBusinessType());
        vo.setBusinessId(entity.getBusinessId());
        vo.setSourceModule(entity.getSourceModule());
        vo.setLink(entity.getLink());
        vo.setCreatedAt(timeMapper.toOffsetDateTime(entity.getCreatedAt()));
        return vo;
    }
}

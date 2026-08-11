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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.notification.javabean.entity.NotificationUserPreferenceEntity;
import com.devops00.spectra.notification.javabean.from.NotificationSettingFrom;
import com.devops00.spectra.notification.javabean.vo.NotificationSettingVO;
import com.devops00.spectra.notification.mapper.NotificationUserPreferenceMapper;
import com.devops00.spectra.notification.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户用途×渠道偏好服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    /** 不允许用户关闭或免打扰的安全用途。 */
    private static final List<String> MANDATORY_PURPOSES = List.of("LOGIN_CODE", "BIND_PHONE_CODE", "BIND_EMAIL_CODE",
            "RESET_PASSWORD_CODE", "SECURITY_ALERT");

    /** 用户通知偏好 Mapper。 */
    private final NotificationUserPreferenceMapper mapper;

    /** 查询指定用户的用途与渠道偏好。 */
    @Override
    public List<NotificationUserPreferenceEntity> list(UUID tenantId, UUID userId) {
        return mapper
                .selectList(new LambdaQueryWrapper<NotificationUserPreferenceEntity>().eq(NotificationUserPreferenceEntity::getTenantId, tenantId)
                        .eq(NotificationUserPreferenceEntity::getUserId, userId)
                        .orderByAsc(NotificationUserPreferenceEntity::getPurpose)
                        .orderByAsc(NotificationUserPreferenceEntity::getChannel));
    }

    /** 保存单个用途与渠道的偏好，并强制保护安全用途。 */
    @Override
    @Transactional
    public void save(UUID tenantId, UUID userId, String purpose, String channel, boolean enabled, boolean doNotDisturb) {
        if (tenantId == null || userId == null || !StringUtils.hasText(purpose) || !StringUtils.hasText(channel)) {
            throw new DataSaveException("通知偏好参数不完整");
        }
        var normalizedPurpose = purpose.toUpperCase();
        var normalizedChannel = channel.toUpperCase();
        try {
            NotificationPurpose.valueOf(normalizedPurpose);
            NotificationChannel.valueOf(normalizedChannel);
        } catch (IllegalArgumentException exception) {
            throw new DataSaveException("通知用途或渠道不合法");
        }
        if (MANDATORY_PURPOSES.contains(normalizedPurpose)) {
            enabled = true;
            doNotDisturb = false;
        }
        var query = new LambdaQueryWrapper<NotificationUserPreferenceEntity>().eq(NotificationUserPreferenceEntity::getTenantId, tenantId)
                .eq(NotificationUserPreferenceEntity::getUserId, userId)
                .eq(NotificationUserPreferenceEntity::getPurpose, normalizedPurpose)
                .eq(NotificationUserPreferenceEntity::getChannel, normalizedChannel);
        var entity = mapper.selectOne(query);
        var existing = entity != null;
        if (!existing) {
            entity = new NotificationUserPreferenceEntity();
            entity.setId(UUID.randomUUID());
            entity.setTenantId(tenantId);
            entity.setUserId(userId);
            entity.setPurpose(normalizedPurpose);
            entity.setChannel(normalizedChannel);
            entity.setCreatedAt(Instant.now());
        }
        entity.setEnabled(enabled);
        entity.setDoNotDisturb(doNotDisturb);
        entity.setUpdatedAt(Instant.now());
        if (existing) {
            if (mapper.updateById(entity) != 1) {
                throw new DataSaveException("保存通知偏好失败");
            }
        } else {
            if (mapper.insert(entity) != 1) {
                throw new DataSaveException("保存通知偏好失败");
            }
        }
    }

    /** 将用途与渠道偏好聚合为旧消息中心设置结构。 */
    @Override
    public NotificationSettingVO legacy(UUID tenantId, UUID userId) {
        var result = new NotificationSettingVO();
        result.setUserId(userId);
        var values = list(tenantId, userId).stream()
                .filter(item -> "IN_APP".equals(item.getChannel()))
                .collect(java.util.stream.Collectors.toMap(NotificationUserPreferenceEntity::getPurpose,
                        item -> Boolean.TRUE.equals(item.getEnabled()), (left, right) -> right));
        result.setSystemEnabled(values.getOrDefault("SYSTEM_NOTICE", true));
        result.setWorkflowEnabled(values.getOrDefault("WORKFLOW_TODO", true));
        result.setOaEnabled(values.getOrDefault("OA_NOTICE", true));
        result.setInnerMailEnabled(values.getOrDefault("INNER_MESSAGE", true));
        result.setApprovalEnabled(values.getOrDefault("WORKFLOW_RESULT", true));
        result.setDoNotDisturb(list(tenantId, userId).stream().anyMatch(item -> Boolean.TRUE.equals(item.getDoNotDisturb())));
        return result;
    }

    /** 将旧消息中心设置展开保存为站内信用途偏好。 */
    @Override
    @Transactional
    public void saveLegacy(UUID tenantId, UUID userId, NotificationSettingFrom from) {
        if (from == null) {
            throw new DataSaveException("通知设置不能为空");
        }
        var doNotDisturb = Boolean.TRUE.equals(from.getDoNotDisturb());
        save(tenantId, userId, "SYSTEM_NOTICE", "IN_APP", Boolean.TRUE.equals(from.getSystemEnabled()), doNotDisturb);
        save(tenantId, userId, "WORKFLOW_TODO", "IN_APP", Boolean.TRUE.equals(from.getWorkflowEnabled()), doNotDisturb);
        save(tenantId, userId, "OA_NOTICE", "IN_APP", Boolean.TRUE.equals(from.getOaEnabled()), doNotDisturb);
        save(tenantId, userId, "INNER_MESSAGE", "IN_APP", Boolean.TRUE.equals(from.getInnerMailEnabled()), doNotDisturb);
        save(tenantId, userId, "WORKFLOW_RESULT", "IN_APP", Boolean.TRUE.equals(from.getApprovalEnabled()), doNotDisturb);
    }
}

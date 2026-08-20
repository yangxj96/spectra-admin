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
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.notification.javabean.entity.NotificationUserPreferenceEntity;
import com.devops00.spectra.notification.mapper.NotificationUserPreferenceMapper;
import com.devops00.spectra.notification.service.NotificationPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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

    /**
     * 不允许用户关闭或免打扰的安全用途。
     */
    private static final List<String> MANDATORY_PURPOSES = List.of("LOGIN_CODE", "BIND_PHONE_CODE", "BIND_EMAIL_CODE",
            "RESET_PASSWORD_CODE", "SECURITY_ALERT");

    /**
     * 用户通知偏好 Mapper。
     */
    private final NotificationUserPreferenceMapper mapper;

    /**
     * 查询指定用户的用途与渠道偏好。
     */
    @Override
    public List<NotificationUserPreferenceEntity> list(UUID userId) {
        return mapper.selectList(new LambdaQueryWrapper<NotificationUserPreferenceEntity>()
                .eq(NotificationUserPreferenceEntity::getUserId, userId)
                .orderByAsc(NotificationUserPreferenceEntity::getPurpose)
                .orderByAsc(NotificationUserPreferenceEntity::getChannel));
    }

    /**
     * 保存单个用途与渠道的偏好，并强制保护安全用途。
     */
    @Override
    @Transactional
    public void save(UUID userId, String purpose, String channel, boolean enabled, boolean doNotDisturb) {
        save(userId, purpose, channel, enabled, doNotDisturb, null, null);
    }

    /**
     * 保存带每日免打扰窗口的用途与渠道偏好，并强制保护安全用途。
     */
    @Override
    @Transactional
    public void save(UUID userId, String purpose, String channel, boolean enabled, boolean doNotDisturb,
                     Instant doNotDisturbStart, Instant doNotDisturbEnd) {
        if (userId == null || !StringUtils.hasText(purpose) || !StringUtils.hasText(channel)) {
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
            doNotDisturbStart = null;
            doNotDisturbEnd = null;
        }
        if (!doNotDisturb) {
            doNotDisturbStart = null;
            doNotDisturbEnd = null;
        }
        var query = new LambdaQueryWrapper<NotificationUserPreferenceEntity>()
                .eq(NotificationUserPreferenceEntity::getUserId, userId)
                .eq(NotificationUserPreferenceEntity::getPurpose, normalizedPurpose)
                .eq(NotificationUserPreferenceEntity::getChannel, normalizedChannel);
        var entity = mapper.selectOne(query);
        var existing = entity != null;
        if (!existing) {
            entity = new NotificationUserPreferenceEntity();
            entity.setId(UUID.randomUUID());
            entity.setUserId(userId);
            entity.setPurpose(normalizedPurpose);
            entity.setChannel(normalizedChannel);
            entity.setCreatedAt(Instant.now());
        }
        entity.setEnabled(enabled);
        entity.setDoNotDisturb(doNotDisturb);
        entity.setDoNotDisturbStart(doNotDisturbStart);
        entity.setDoNotDisturbEnd(doNotDisturbEnd);
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

}

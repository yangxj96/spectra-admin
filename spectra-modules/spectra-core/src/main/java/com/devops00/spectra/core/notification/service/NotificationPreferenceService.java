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

package com.devops00.spectra.core.notification.service;

import com.devops00.spectra.core.notification.javabean.entity.NotificationUserPreferenceEntity;

import java.util.List;
import java.time.Instant;
import java.util.UUID;

/**
 * 用户用途×渠道偏好服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
public interface NotificationPreferenceService {

    /**
     * 查询用户偏好。
     *
     * @param userId 目标用户的唯一标识，用于限定查询或变更范围。
     * @return 返回符合查询条件的用户通知偏好列表；无匹配时返回空列表，不返回 null。
     */
    List<NotificationUserPreferenceEntity> list(UUID userId);

    /**
     * 保存可选用途偏好；安全用途由调用策略强制开启。
     *
     * @param userId       目标用户的唯一标识，用于限定查询或变更范围。
     * @param purpose      通知用途，用于应用通知偏好和模板策略。
     * @param channel      通知投递渠道，用于选择站内信、短信或邮件等发送路径。
     * @param enabled      是否启用该功能或通知渠道。
     * @param doNotDisturb 是否开启免打扰策略。
     */
    void save(UUID userId, String purpose, String channel, boolean enabled, boolean doNotDisturb);

    /**
     * 保存带每日免打扰窗口的用途与渠道偏好。
     *
     * @param userId            目标用户的唯一标识，用于限定查询或变更范围。
     * @param purpose           通知用途，用于应用通知偏好和模板策略。
     * @param channel           通知投递渠道，用于选择站内信、短信或邮件等发送路径。
     * @param enabled           是否启用该功能或通知渠道。
     * @param doNotDisturb      是否开启免打扰策略。
     * @param doNotDisturbStart 免打扰开始时间。
     * @param doNotDisturbEnd   免打扰结束时间。
     */
    void save(UUID userId, String purpose, String channel, boolean enabled, boolean doNotDisturb,
              Instant doNotDisturbStart, Instant doNotDisturbEnd);

}

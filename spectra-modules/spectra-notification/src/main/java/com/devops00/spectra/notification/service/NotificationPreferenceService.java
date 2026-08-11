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

package com.devops00.spectra.notification.service;

import java.util.List;
import java.util.UUID;

import com.devops00.spectra.notification.javabean.entity.NotificationUserPreferenceEntity;
import com.devops00.spectra.notification.javabean.from.NotificationSettingFrom;
import com.devops00.spectra.notification.javabean.vo.NotificationSettingVO;

/**
 * 用户用途×渠道偏好服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
public interface NotificationPreferenceService {

    /** 查询用户偏好。 */
    List<NotificationUserPreferenceEntity> list(UUID tenantId, UUID userId);

    /** 保存可选用途偏好；安全用途由调用策略强制开启。 */
    void save(UUID tenantId, UUID userId, String purpose, String channel, boolean enabled, boolean doNotDisturb);

    /** 读取旧消息中心设置结构，供兼容 API 使用。 */
    NotificationSettingVO legacy(UUID tenantId, UUID userId);

    /** 保存旧消息中心设置结构，内部展开为用途×渠道记录。 */
    void saveLegacy(UUID tenantId, UUID userId, NotificationSettingFrom from);
}

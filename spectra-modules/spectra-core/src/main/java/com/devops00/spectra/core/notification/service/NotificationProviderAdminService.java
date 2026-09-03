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

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.core.notification.javabean.domain.NotificationProviderConfiguration;
import com.devops00.spectra.core.notification.javabean.from.NotificationProviderSaveFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationProviderVO;

import java.util.List;

/**
 * 通知 Provider 配置管理服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
public interface NotificationProviderAdminService {

    /**
     * 查询所有渠道的脱敏配置。
     */
    List<NotificationProviderVO> list();

    /**
     * 查询指定渠道的脱敏配置。
     */
    NotificationProviderVO get(NotificationChannel channel);

    /**
     * 读取 Provider 运行时配置；Secret 仅供 Provider 内部使用。
     */
    NotificationProviderConfiguration resolve(NotificationChannel channel);

    /**
     * 保存指定渠道配置。
     */
    NotificationProviderVO modify(NotificationChannel channel, NotificationProviderSaveFrom params);
}

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

import com.devops00.spectra.notification.javabean.domain.ChannelSendResult;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderConfiguration;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderHealth;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;

/**
 * 外部通知 Provider SPI；Gateway、Worker 不感知供应商 SDK 或 HTTP 细节。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
public interface NotificationProvider {

    /**
     * Provider 类型编码。
     */
    String code();

    /**
     * 执行健康检查；不得返回明文响应或凭据。
     */
    NotificationProviderHealth health(NotificationProviderConfiguration configuration);

    /**
     * 发送一个已经由通知域渲染并保护地址的任务。
     */
    ChannelSendResult send(NotificationTaskEntity task, NotificationProviderConfiguration configuration);
}

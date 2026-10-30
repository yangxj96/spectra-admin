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

package com.devops00.spectra.core.notification.sender;

import com.devops00.spectra.common.notification.NotificationChannel;

/**
 * 单一渠道的同步发送端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
public interface NotificationSender {

    /**
     * 当前发送器支持的渠道。
     *
     * @return 通知渠道
     */
    NotificationChannel channel();

    /**
     * 同步发送一条已渲染通知。
     *
     * @param target  发送目标
     * @param message 已渲染内容
     * @return 发送结果
     */
    SendOutcome send(NotificationSendTarget target, RenderedMessage message);
}

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

package com.devops00.spectra.notification.dispatch.service;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.dispatch.javabean.bo.ChannelSendResult;

/** 单一通知渠道的发送端口。 */
public interface NotificationSender {

    /** 当前 Sender 支持的渠道。 */
    NotificationChannel channel();

    /** 当前 Sender 是否已配置为可用。 */
    default boolean available() {
        return true;
    }

    /** 不可用时返回脱敏原因。 */
    default String unavailableReason() {
        return "CHANNEL_NOT_CONFIGURED";
    }

    /** 执行一次投递；不得记录明文敏感载荷。 */
    ChannelSendResult send(NotificationTaskEntity task);
}

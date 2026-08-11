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

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.javabean.domain.ChannelSendResult;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.service.NotificationSender;
import org.springframework.stereotype.Component;

/**
 * 邮件占位 Sender；未接入真实供应商时明确返回未配置。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Component
public class PlaceholderEmailSender implements NotificationSender {

    /** 返回邮件渠道标识。 */
    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    /** 邮件供应商尚未配置。 */
    @Override
    public boolean available() {
        return false;
    }

    /** 返回邮件渠道未配置原因。 */
    @Override
    public String unavailableReason() {
        return "EMAIL_CHANNEL_NOT_CONFIGURED";
    }

    /** 明确阻断投递，不伪造邮件发送成功。 */
    @Override
    public ChannelSendResult send(NotificationTaskEntity task) {
        return new ChannelSendResult("BLOCKED", "EMAIL_PLACEHOLDER", null, "CHANNEL_NOT_CONFIGURED");
    }
}

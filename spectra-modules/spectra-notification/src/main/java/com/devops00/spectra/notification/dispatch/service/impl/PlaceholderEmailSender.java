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

package com.devops00.spectra.notification.dispatch.service.impl;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.dispatch.javabean.bo.ChannelSendResult;
import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.dispatch.service.NotificationSender;
import org.springframework.stereotype.Component;

/** 邮件占位 Sender；未接入真实供应商时明确返回未配置。 */
@Component
public class PlaceholderEmailSender implements NotificationSender {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public ChannelSendResult send(NotificationTaskEntity task) {
        return new ChannelSendResult("BLOCKED", "EMAIL_PLACEHOLDER", null, "CHANNEL_NOT_CONFIGURED");
    }
}

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

package com.devops00.spectra.core.notification.sender.impl;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.core.notification.javabean.domain.ChannelSendResult;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.core.notification.provider.NotificationProviderRuntime;
import com.devops00.spectra.core.notification.sender.NotificationSender;

/**
 * SMS/EMAIL Sender 的公共适配层；只依赖 Provider Runtime，不依赖具体供应商。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
public abstract class AbstractExternalNotificationSender implements NotificationSender {

    /**
     * Provider Runtime。
     */
    private final NotificationProviderRuntime runtime;

    /**
     * 当前外部渠道。
     */
    private final NotificationChannel channel;

    protected AbstractExternalNotificationSender(NotificationProviderRuntime runtime, NotificationChannel channel) {
        this.runtime = runtime;
        this.channel = channel;
    }

    @Override
    public NotificationChannel channel() {
        return channel;
    }

    @Override
    public boolean available() {
        return runtime.available(channel);
    }

    @Override
    public String unavailableReason() {
        return runtime.unavailableReason(channel);
    }

    @Override
    public ChannelSendResult send(NotificationTaskEntity task) {
        return runtime.send(channel, task);
    }
}

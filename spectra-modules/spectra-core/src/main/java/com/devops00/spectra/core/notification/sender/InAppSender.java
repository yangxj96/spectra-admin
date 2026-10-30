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
import org.springframework.stereotype.Component;

/**
 * 站内信发送器；历史记录由通知服务同事务写入，因此这里只需返回成功。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Component
public class InAppSender implements NotificationSender {

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public SendOutcome send(NotificationSendTarget target, RenderedMessage message) {
        return SendOutcome.success("IN_APP", null);
    }
}

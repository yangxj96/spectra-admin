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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.javabean.domain.ChannelSendResult;
import com.devops00.spectra.notification.javabean.entity.NotificationInboxEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.mapper.NotificationInboxMapper;
import com.devops00.spectra.notification.service.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 站内信 Sender；taskId 是天然幂等键。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Component
@RequiredArgsConstructor
public class InAppNotificationSender implements NotificationSender {

    /**
     * 站内信收件箱 Mapper。
     */
    private final NotificationInboxMapper inboxMapper;

    /**
     * 返回站内信渠道标识。
     */
    @Override
    public NotificationChannel channel() {
        return NotificationChannel.IN_APP;
    }

    /**
     * 幂等写入收件箱，并返回成功结果。
     */
    @Override
    public ChannelSendResult send(NotificationTaskEntity task) {
        var exists = inboxMapper.selectOne(new LambdaQueryWrapper<NotificationInboxEntity>()
                .eq(NotificationInboxEntity::getNotificationTaskId, task.getId()));
        if (exists == null) {
            var inbox = new NotificationInboxEntity();
            inbox.setReceiverUserId(task.getReceiverUserId());
            inbox.setNotificationRequestId(task.getNotificationRequestId());
            inbox.setNotificationTaskId(task.getId());
            inbox.setPurpose(task.getPurpose());
            inbox.setTitle(task.getTitle());
            inbox.setContent(task.getContent());
            inbox.setLink(task.getLink());
            inbox.setExtra(task.getExtra());
            inbox.setIsRead(false);
            if (inboxMapper.insert(inbox) != 1) {
                throw new DataSaveException("写入站内信失败");
            }
        }
        return new ChannelSendResult("SENT", "IN_APP", null, "站内信已写入收件箱");
    }
}

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

import java.time.Instant;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.dispatch.javabean.bo.ChannelSendResult;
import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.inbox.javabean.entity.NotificationInboxEntity;
import com.devops00.spectra.notification.inbox.mapper.NotificationInboxMapper;
import com.devops00.spectra.notification.dispatch.service.NotificationSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 站内信 Sender；taskId 是天然幂等键。 */
@Component
@RequiredArgsConstructor
public class InAppNotificationSender implements NotificationSender {

    private final NotificationInboxMapper inboxMapper;

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.IN_APP;
    }

    @Override
    public ChannelSendResult send(NotificationTaskEntity task) {
        var exists = inboxMapper.selectOne(new LambdaQueryWrapper<NotificationInboxEntity>()
                .eq(NotificationInboxEntity::getTaskId, task.getId()));
        if (exists == null) {
            var inbox = new NotificationInboxEntity();
            inbox.setId(UUID.randomUUID());
            inbox.setTenantId(task.getTenantId());
            inbox.setRecipientUserId(task.getRecipientUserId());
            inbox.setRequestId(task.getRequestId());
            inbox.setTaskId(task.getId());
            inbox.setPurpose(task.getPurpose());
            inbox.setTitle(task.getTitle());
            inbox.setContent(task.getContent());
            inbox.setLink(task.getLink());
            inbox.setExtra(task.getExtra());
            inbox.setCreatedAt(Instant.now());
            if (inboxMapper.insert(inbox) != 1) {
                throw new DataSaveException("写入站内信失败");
            }
        }
        return new ChannelSendResult("SENT", "IN_APP", null, "站内信已写入收件箱");
    }
}

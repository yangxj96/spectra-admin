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

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.mybatis.handler.UUIDTypeHandler;
import com.devops00.spectra.notification.javabean.entity.NotificationInboxEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.mapper.NotificationInboxMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 通知渠道 Sender 的幂等和占位行为测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/13
 */
class NotificationSenderTest {

    @BeforeAll
    static void initializeMybatisLambdaMetadata() {
        var configuration = new MybatisConfiguration();
        configuration.getTypeHandlerRegistry().register(UUID.class, UUIDTypeHandler.class);
        var assistant = new MapperBuilderAssistant(configuration, "notification-sender-test");
        TableInfoHelper.initTableInfo(assistant, NotificationInboxEntity.class);
    }

    @Test
    void shouldReturnBlockedResultForPlaceholderChannels() {
        var task = new NotificationTaskEntity();
        task.setId(UUID.randomUUID());

        var sms = new PlaceholderSmsSender();
        var email = new PlaceholderEmailSender();

        assertEquals(NotificationChannel.SMS, sms.channel());
        assertEquals(NotificationChannel.EMAIL, email.channel());
        assertEquals("BLOCKED", sms.send(task).status());
        assertEquals("BLOCKED", email.send(task).status());
        assertEquals("CHANNEL_NOT_CONFIGURED", sms.send(task).summary());
        assertEquals("CHANNEL_NOT_CONFIGURED", email.send(task).summary());
        assertEquals("SMS_CHANNEL_NOT_CONFIGURED", sms.unavailableReason());
        assertEquals("EMAIL_CHANNEL_NOT_CONFIGURED", email.unavailableReason());
    }

    @Test
    void shouldTreatExistingTaskInboxMessageAsIdempotent() {
        var mapper = mock(NotificationInboxMapper.class);
        var task = new NotificationTaskEntity();
        task.setId(UUID.randomUUID());
        when(mapper.selectOne(any())).thenReturn(null, new NotificationInboxEntity());
        when(mapper.insert(any(NotificationInboxEntity.class))).thenReturn(1);

        var sender = new InAppNotificationSender(mapper);
        var firstResult = sender.send(task);
        var secondResult = sender.send(task);

        assertEquals("SENT", firstResult.status());
        assertEquals("SENT", secondResult.status());
        assertEquals("IN_APP", secondResult.providerCode());
        verify(mapper).insert(any(NotificationInboxEntity.class));
    }
}

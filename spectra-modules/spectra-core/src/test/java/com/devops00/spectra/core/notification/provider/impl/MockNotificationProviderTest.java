/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.notification.provider.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.devops00.spectra.core.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.core.notification.javabean.domain.ChannelSendStatus;
import com.devops00.spectra.core.notification.javabean.domain.NotificationProviderConfiguration;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTaskEntity;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证内置模拟渠道只记录安全字段，不泄露验证码和直接地址。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/18
 */
class MockNotificationProviderTest {

    @Test
    void acceptedDeliveryDoesNotLogSensitiveContent() {
        var protector = mock(NotificationPayloadProtector.class);
        when(protector.unprotectAddress("ciphertext")).thenReturn("secret-user@example.test");
        var configuration = mock(NotificationProviderConfiguration.class);
        when(configuration.enabled()).thenReturn(true);
        when(configuration.providerType()).thenReturn("MOCK");
        var task = new NotificationTaskEntity();
        task.setId(UUID.randomUUID());
        task.setChannel("EMAIL");
        task.setRecipientCiphertext("ciphertext");
        task.setRecipientMasked("untrusted-recipient-marker");
        task.setTitle("private-title-marker");
        task.setContent("private-code-654321");

        var logger = (Logger) LoggerFactory.getLogger(MockNotificationProvider.class);
        var priorLevel = logger.getLevel();
        var appender = new ListAppender<ILoggingEvent>();
        appender.start();
        logger.addAppender(appender);
        logger.setLevel(Level.INFO);
        try {
            var result = new MockNotificationProvider(protector).send(task, configuration);
            assertEquals(ChannelSendStatus.SENT, result.status());
            assertEquals("MOCK_ACCEPTED", result.summary());
            assertEquals(1, appender.list.size());
            String message = appender.list.getFirst().getFormattedMessage();
            assertTrue(message.contains("result=MOCK_ACCEPTED"));
            assertTrue(message.contains(task.getId().toString()));
            assertFalse(message.contains("secret-user@example.test"));
            assertFalse(message.contains("untrusted-recipient-marker"));
            assertFalse(message.contains("private-title-marker"));
            assertFalse(message.contains("private-code-654321"));
        } finally {
            logger.detachAppender(appender);
            logger.setLevel(priorLevel);
            appender.stop();
        }
    }

    @Test
    void unavailableConfigurationAndUnreadableAddressAreRejected() {
        var protector = mock(NotificationPayloadProtector.class);
        var provider = new MockNotificationProvider(protector);
        var task = new NotificationTaskEntity();
        task.setRecipientCiphertext("ciphertext");
        assertEquals(ChannelSendStatus.BLOCKED, provider.send(task, null).status());

        var configuration = mock(NotificationProviderConfiguration.class);
        when(configuration.enabled()).thenReturn(true);
        when(configuration.providerType()).thenReturn("MOCK");
        when(protector.unprotectAddress("ciphertext")).thenThrow(new IllegalStateException("synthetic decrypt failure"));
        assertEquals("RECIPIENT_ADDRESS_UNAVAILABLE", provider.send(task, configuration).summary());
    }
}

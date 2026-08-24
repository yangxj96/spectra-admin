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

package com.devops00.spectra.notification.provider.impl;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.notification.javabean.domain.ChannelSendStatus;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderConfiguration;
import com.devops00.spectra.notification.javabean.domain.NotificationProviderHealthState;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.properties.NotificationModuleProperties;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 内置 SMS/EMAIL Mock Provider 测试。
 */
class MockProviderTest {

    @Test
    void shouldUseNormalProviderGateAndPrintOnlyAtFinalSendPoint() {
        var key = Base64.getEncoder().encodeToString(new byte[32]);
        var protector = new NotificationPayloadProtector(
                new NotificationModuleProperties(true, key, key, List.of()), new ObjectMapper());
        var provider = new MockNotificationProvider(protector);
        var task = new NotificationTaskEntity();
        task.setId(UUID.randomUUID());
        task.setChannel("SMS");
        task.setRecipientCiphertext(protector.protectAddress("13800138000"));
        task.setRecipientMasked("138****0000");
        task.setTitle("测试标题");
        task.setContent("测试正文");

        var configuration = new NotificationProviderConfiguration(NotificationChannel.SMS, "MOCK", true, "", 0,
                null, null, null, null, null, null, false, false, 2_000, 10, 3, null, null, null, null,
                Instant.now());

        assertEquals("SMS", NotificationChannel.SMS.name());
        assertEquals(NotificationProviderHealthState.HEALTHY, provider.health(configuration).state());
        var result = provider.send(task, configuration);
        assertEquals(ChannelSendStatus.SENT, result.status());
        assertEquals("MOCK", result.providerCode());
        assertEquals("mock-" + task.getId(), result.providerMessageId());
        assertEquals("MOCK_ACCEPTED", result.summary());
    }
}

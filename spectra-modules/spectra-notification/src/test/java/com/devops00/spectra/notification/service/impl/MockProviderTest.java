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

import java.util.UUID;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** SMS/EMAIL Provider 测试替身示例。 */
class MockProviderTest {

    @Test
    void shouldReturnDeterministicAcceptedResultsWithoutExternalCalls() {
        var task = new NotificationTaskEntity();
        task.setId(UUID.randomUUID());

        var sms = new MockSmsSender().send(task);
        var email = new MockEmailSender().send(task);

        assertEquals(NotificationChannel.SMS, new MockSmsSender().channel());
        assertEquals(NotificationChannel.EMAIL, new MockEmailSender().channel());
        assertEquals("SENT", sms.status());
        assertEquals("MOCK_SMS", sms.providerCode());
        assertEquals("mock-sms-" + task.getId(), sms.providerMessageId());
        assertEquals("SENT", email.status());
        assertEquals("MOCK_EMAIL", email.providerCode());
        assertEquals("mock-email-" + task.getId(), email.providerMessageId());
    }
}

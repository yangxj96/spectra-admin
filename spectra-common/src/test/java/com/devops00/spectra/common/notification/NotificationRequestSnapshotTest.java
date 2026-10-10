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

package com.devops00.spectra.common.notification;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 快捷通知输入在入队前也必须持有稳定的集合快照。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/10
 */
class NotificationRequestSnapshotTest {

    @Test
    void inAppInputKeepsRecipientSnapshotAndNormalizesMissingRecipients() {
        var recipientId = UUID.randomUUID();
        var recipients = new ArrayList<>(List.of(recipientId));
        var input = new NotificationRequest.InAppInput("synthetic-in-app", NotificationPurpose.SYSTEM_NOTICE,
                recipients, NotificationTemplateCode.SYSTEM_SERVICE_MONITOR_ALERT, "title", "content",
                null, null, "spectra-core", null);

        recipients.add(UUID.randomUUID());
        assertEquals(List.of(recipientId), input.recipientUserIds());
        assertEquals(List.of(recipientId), NotificationRequest.inApp(input).recipientUserIds());
        assertThrows(UnsupportedOperationException.class, () -> input.recipientUserIds().add(UUID.randomUUID()));

        var missing = new NotificationRequest.InAppInput("synthetic-empty", NotificationPurpose.SYSTEM_NOTICE,
                null, NotificationTemplateCode.SYSTEM_SERVICE_MONITOR_ALERT, null, null,
                null, null, "spectra-core", null);
        assertEquals(List.of(), missing.recipientUserIds());
    }

    @Test
    void userRequestSnapshotsRecipientsChannelsAndParameters() {
        var recipientId = UUID.randomUUID();
        var recipients = new ArrayList<>(List.of(recipientId));
        var channels = new ArrayList<NotificationChannel>();
        channels.add(NotificationChannel.IN_APP);
        channels.add(null);
        var parameters = new HashMap<String, Object>();
        parameters.put("title", "before");
        var request = new NotificationService.UserNotificationRequest("synthetic-user",
                NotificationPurpose.SYSTEM_NOTICE, recipients, channels,
                NotificationTemplateCode.SYSTEM_SERVICE_MONITOR_ALERT, parameters);

        recipients.add(UUID.randomUUID());
        channels.add(NotificationChannel.EMAIL);
        parameters.put("title", "after");
        assertEquals(List.of(recipientId), request.recipientUserIds());
        assertEquals(List.of(NotificationChannel.IN_APP), request.channels());
        assertEquals("before", request.parameters().get("title"));
        assertThrows(UnsupportedOperationException.class, () -> request.recipientUserIds().clear());
        assertThrows(UnsupportedOperationException.class, () -> request.channels().clear());
        assertThrows(UnsupportedOperationException.class, () -> request.parameters().clear());
    }

    @Test
    void directRequestSnapshotsAddressesAndBothParameterMaps() {
        var address = new NotificationDirectAddress(NotificationChannel.EMAIL, "synthetic@example.invalid");
        var addresses = new ArrayList<NotificationDirectAddress>();
        addresses.add(address);
        addresses.add(null);
        var parameters = new HashMap<String, Object>();
        parameters.put("title", "before");
        var sensitiveParameters = new HashMap<String, Object>();
        sensitiveParameters.put("code", "synthetic-code");
        var request = new NotificationService.DirectNotificationRequest("synthetic-direct",
                NotificationPurpose.LOGIN_CODE, addresses, "synthetic-template", parameters, sensitiveParameters);

        addresses.add(new NotificationDirectAddress(NotificationChannel.SMS, "00000000000"));
        parameters.put("title", "after");
        sensitiveParameters.put("code", "changed-code");
        assertEquals(List.of(address), request.directAddresses());
        assertEquals("before", request.parameters().get("title"));
        assertEquals("synthetic-code", request.sensitiveParameters().get("code"));
        assertThrows(UnsupportedOperationException.class, () -> request.directAddresses().clear());
        assertThrows(UnsupportedOperationException.class, () -> request.parameters().clear());
        assertThrows(UnsupportedOperationException.class, () -> request.sensitiveParameters().clear());
    }

    @Test
    void concurrentCallerMutationCannotChangeConstructedSnapshots() throws Exception {
        var recipientId = UUID.randomUUID();
        var recipients = new ArrayList<>(List.of(recipientId));
        var address = new NotificationDirectAddress(NotificationChannel.EMAIL, "synthetic@example.invalid");
        var addresses = new ArrayList<>(List.of(address));
        var parameters = new HashMap<String, Object>();
        parameters.put("title", "before");
        var sensitiveParameters = new HashMap<String, Object>();
        sensitiveParameters.put("code", "before-code");
        var input = new NotificationRequest.InAppInput("synthetic-race", NotificationPurpose.SYSTEM_NOTICE,
                recipients, NotificationTemplateCode.SYSTEM_SERVICE_MONITOR_ALERT, null, null,
                null, null, "spectra-core", null);
        var user = new NotificationService.UserNotificationRequest("synthetic-race",
                NotificationPurpose.SYSTEM_NOTICE, recipients, List.of(NotificationChannel.IN_APP),
                NotificationTemplateCode.SYSTEM_SERVICE_MONITOR_ALERT, parameters);
        var direct = new NotificationService.DirectNotificationRequest("synthetic-race",
                NotificationPurpose.LOGIN_CODE, addresses, "synthetic-template", parameters, sensitiveParameters);
        var start = new CountDownLatch(1);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var mutation = executor.submit(() -> {
                start.await();
                for (int index = 0; index < 100; index++) {
                    recipients.add(UUID.randomUUID());
                    addresses.add(new NotificationDirectAddress(NotificationChannel.EMAIL, "other@example.invalid"));
                    parameters.put("title", "after");
                    sensitiveParameters.put("code", "after-code");
                }
                return null;
            });
            var observation = executor.submit(() -> {
                start.await();
                for (int index = 0; index < 100; index++) {
                    assertEquals(List.of(recipientId), input.recipientUserIds());
                    assertEquals(List.of(recipientId), user.recipientUserIds());
                    assertEquals(List.of(address), direct.directAddresses());
                    assertEquals("before", user.parameters().get("title"));
                    assertEquals("before", direct.parameters().get("title"));
                    assertEquals("before-code", direct.sensitiveParameters().get("code"));
                }
                return null;
            });
            start.countDown();
            mutation.get();
            observation.get();
        }
    }

    @Test
    void shortcutRequestsNormalizeMissingCollectionsAndRejectNullMapValues() {
        var user = new NotificationService.UserNotificationRequest("synthetic-user",
                NotificationPurpose.SYSTEM_NOTICE, null, null,
                NotificationTemplateCode.SYSTEM_SERVICE_MONITOR_ALERT, null);
        assertEquals(List.of(), user.recipientUserIds());
        assertEquals(List.of(), user.channels());
        assertEquals(java.util.Map.of(), user.parameters());

        var direct = new NotificationService.DirectNotificationRequest("synthetic-direct",
                NotificationPurpose.LOGIN_CODE, null, "synthetic-template", null, null);
        assertEquals(List.of(), direct.directAddresses());
        assertEquals(java.util.Map.of(), direct.parameters());
        assertEquals(java.util.Map.of(), direct.sensitiveParameters());

        var invalidParameters = new HashMap<String, Object>();
        invalidParameters.put("title", null);
        assertThrows(NullPointerException.class, () -> new NotificationService.UserNotificationRequest(
                "synthetic-invalid", NotificationPurpose.SYSTEM_NOTICE, List.of(), List.of(),
                NotificationTemplateCode.SYSTEM_SERVICE_MONITOR_ALERT, invalidParameters));
    }
}

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

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationChannelAvailability;
import com.devops00.spectra.common.notification.NotificationDirectAddress;
import com.devops00.spectra.common.notification.NotificationGateway;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationReceipt;
import com.devops00.spectra.common.notification.NotificationRequest;
import com.devops00.spectra.common.notification.NotificationSendRequest;
import com.devops00.spectra.common.notification.NotificationTemplateCode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 快捷通知服务请求转换测试。 */
class NotificationServiceImplTest {

    @Test
    void shouldTranslateShortcutRequestToGatewayRequest() {
        var gateway = mock(NotificationGateway.class);
        when(gateway.availability(any(NotificationChannel.class)))
                .thenAnswer(invocation -> new NotificationChannelAvailability(invocation.getArgument(0), true, "AVAILABLE"));
        var receipt = new NotificationReceipt(UUID.randomUUID(), "ACCEPTED", 1, false);
        when(gateway.enqueue(any(NotificationRequest.class))).thenReturn(receipt);
        var service = new NotificationServiceImpl(gateway);
        var userId = UUID.randomUUID();
        var request = NotificationSendRequest.inApp("test:notification:1", NotificationPurpose.SYSTEM_NOTICE,
                List.of(userId), NotificationTemplateCode.SYSTEM_NOTICE)
                .businessReference("TEST", "1")
                .sourceModule("test")
                .build();

        var result = service.send(request);

        assertSame(receipt, result);
        var captor = org.mockito.ArgumentCaptor.forClass(NotificationRequest.class);
        verify(gateway).enqueue(captor.capture());
        assertEquals(request.idempotencyKey(), captor.getValue().idempotencyKey());
        assertEquals(request.templateGroupCode(), captor.getValue().templateGroupCode());
        assertEquals(request.recipientUserIds(), captor.getValue().recipientUserIds());
        assertEquals(request.parameters(), captor.getValue().parameters());
        assertEquals(request.businessType(), captor.getValue().businessType());
    }

    @Test
    void shouldAllowShortcutRequestWithoutTemplateParameters() {
        var gateway = mock(NotificationGateway.class);
        when(gateway.availability(any(NotificationChannel.class)))
                .thenAnswer(invocation -> new NotificationChannelAvailability(invocation.getArgument(0), true, "AVAILABLE"));
        when(gateway.enqueue(any(NotificationRequest.class)))
                .thenReturn(new NotificationReceipt(UUID.randomUUID(), "ACCEPTED", 1, false));
        var service = new NotificationServiceImpl(gateway);

        service.send(NotificationSendRequest.inApp("test:notification:empty", NotificationPurpose.SYSTEM_NOTICE,
                List.of(UUID.randomUUID()), NotificationTemplateCode.SYSTEM_NOTICE)
                .build());

        var captor = org.mockito.ArgumentCaptor.forClass(NotificationRequest.class);
        verify(gateway).enqueue(captor.capture());
        assertEquals(Map.of(), captor.getValue().parameters());
    }

    @Test
    void shouldProvideSingleAndMultipleChannelShortcuts() {
        var gateway = mock(NotificationGateway.class);
        when(gateway.availability(any(NotificationChannel.class)))
                .thenAnswer(invocation -> new NotificationChannelAvailability(invocation.getArgument(0), true, "AVAILABLE"));
        when(gateway.enqueue(any(NotificationRequest.class)))
                .thenReturn(new NotificationReceipt(UUID.randomUUID(), "ACCEPTED", 1, false));
        var service = new NotificationServiceImpl(gateway);
        var userId = UUID.randomUUID();

        service.sendInApp("test:in-app", NotificationPurpose.SYSTEM_NOTICE, List.of(userId),
                NotificationTemplateCode.SYSTEM_NOTICE, Map.of());
        service.sendSms("test:sms", NotificationPurpose.SYSTEM_NOTICE, List.of(userId),
                NotificationTemplateCode.SYSTEM_NOTICE, Map.of());
        service.sendEmail("test:email", NotificationPurpose.SYSTEM_NOTICE, List.of(userId),
                NotificationTemplateCode.SYSTEM_NOTICE, Map.of());
        service.sendToUsers("test:multi", NotificationPurpose.SYSTEM_NOTICE, List.of(userId),
                List.of(NotificationChannel.IN_APP, NotificationChannel.SMS, NotificationChannel.EMAIL),
                NotificationTemplateCode.SYSTEM_NOTICE, Map.of());

        var captor = org.mockito.ArgumentCaptor.forClass(NotificationRequest.class);
        verify(gateway, org.mockito.Mockito.times(4)).enqueue(captor.capture());
        assertEquals(List.of(NotificationChannel.IN_APP), captor.getAllValues().get(0).channels());
        assertEquals(List.of(NotificationChannel.SMS), captor.getAllValues().get(1).channels());
        assertEquals(List.of(NotificationChannel.EMAIL), captor.getAllValues().get(2).channels());
        assertEquals(List.of(NotificationChannel.IN_APP, NotificationChannel.SMS, NotificationChannel.EMAIL),
                captor.getAllValues().get(3).channels());
    }

    @Test
    void shouldBuildSingleAndMultipleDirectChannelAddresses() {
        var single = NotificationSendRequest.direct("test:direct:sms", NotificationPurpose.LOGIN_CODE,
                NotificationChannel.SMS, "13800138000", NotificationTemplateCode.SECURITY_LOGIN_CODE)
                .sensitiveParameter("code", "123456")
                .build();
        var multiple = NotificationSendRequest.direct("test:direct:multi", NotificationPurpose.LOGIN_CODE,
                List.of(new NotificationDirectAddress(NotificationChannel.SMS, "13800138000"),
                        new NotificationDirectAddress(NotificationChannel.EMAIL, "user@example.com")),
                NotificationTemplateCode.SECURITY_LOGIN_CODE)
                .sensitiveParameter("code", "123456")
                .build();

        assertEquals(List.of(NotificationChannel.SMS), single.channels());
        assertEquals(List.of(NotificationChannel.SMS, NotificationChannel.EMAIL), multiple.channels());
        assertEquals(2, multiple.directAddresses().size());
    }

    @Test
    void shouldCheckAllChannelsBeforeEnqueueing() {
        var gateway = mock(NotificationGateway.class);
        when(gateway.availability(NotificationChannel.SMS))
                .thenReturn(new NotificationChannelAvailability(NotificationChannel.SMS, false, "CHANNEL_NOT_CONFIGURED"));
        var service = new NotificationServiceImpl(gateway);

        assertThrows(DataSaveException.class, () -> service.send(NotificationSendRequest.direct(
                "test:direct:unavailable", NotificationPurpose.LOGIN_CODE, NotificationChannel.SMS,
                "13800138000", NotificationTemplateCode.SECURITY_LOGIN_CODE)
                .sensitiveParameter("code", "123456")
                .build()));

        verify(gateway, never()).enqueue(any(NotificationRequest.class));
    }

    @Test
    void shouldRejectNullShortcutRequest() {
        var service = new NotificationServiceImpl(mock(NotificationGateway.class));

        assertThrows(DataSaveException.class, () -> service.send(null));
    }
}

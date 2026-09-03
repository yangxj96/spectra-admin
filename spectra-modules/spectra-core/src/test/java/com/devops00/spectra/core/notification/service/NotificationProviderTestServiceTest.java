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

package com.devops00.spectra.core.notification.service;

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.core.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.core.notification.javabean.domain.ChannelSendResult;
import com.devops00.spectra.core.notification.javabean.from.NotificationProviderTestFrom;
import com.devops00.spectra.core.notification.provider.NotificationProviderRuntime;
import com.devops00.spectra.core.notification.service.impl.NotificationProviderTestServiceImpl;
import com.devops00.spectra.core.notification.support.NotificationTestTimeMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Provider 测试发送的确认、地址保护和结果脱敏边界测试。
 */
class NotificationProviderTestServiceTest {

    @Test
    void shouldRequireExplicitConfirmationBeforeSending() {
        var runtime = mock(NotificationProviderRuntime.class);
        var protector = mock(NotificationPayloadProtector.class);
        var service = new NotificationProviderTestServiceImpl(runtime, protector, NotificationTestTimeMapper.create());
        var params = params();
        params.setConfirmation("SEND");

        assertThrows(DataSaveException.class, () -> service.send(NotificationChannel.SMS, params));
        verify(runtime, never()).send(any(), any());
        verify(protector, never()).protectAddress(any());
    }

    @Test
    void shouldProtectExplicitAddressAndReturnProviderResultWithoutEchoingIt() {
        var runtime = mock(NotificationProviderRuntime.class);
        var protector = mock(NotificationPayloadProtector.class);
        when(protector.protectAddress("+8613800138000")).thenReturn("v1:iv:ciphertext");
        when(runtime.send(any(), any())).thenReturn(ChannelSendResult.sent("HTTP_JSON", "message-1",
                "PROVIDER_ACCEPTED"));
        var service = new NotificationProviderTestServiceImpl(runtime, protector, NotificationTestTimeMapper.create());

        var result = service.send(NotificationChannel.SMS, params());

        assertEquals("SMS", result.getChannel());
        assertEquals("SENT", result.getStatus());
        assertEquals("message-1", result.getProviderMessageId());
        verify(protector).protectAddress("+8613800138000");
    }

    @Test
    void shouldRejectInAppTestSending() {
        var service = new NotificationProviderTestServiceImpl(mock(NotificationProviderRuntime.class),
                mock(NotificationPayloadProtector.class), NotificationTestTimeMapper.create());

        assertThrows(DataSaveException.class, () -> service.send(NotificationChannel.IN_APP, params()));
    }

    private NotificationProviderTestFrom params() {
        var params = new NotificationProviderTestFrom();
        params.setRecipientAddress("+8613800138000");
        params.setTitle("Spectra Provider Test");
        params.setContent("This is a provider test message.");
        params.setConfirmation("SEND_TEST");
        return params;
    }
}

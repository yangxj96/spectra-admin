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

package com.devops00.spectra.notification.request.service.impl;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

import com.devops00.spectra.common.notification.NotificationRecipient;
import com.devops00.spectra.common.notification.NotificationRecipientDirectory;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationRequest;
import com.devops00.spectra.notification.configuration.NotificationModuleProperties;
import com.devops00.spectra.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.notification.dispatch.mapper.NotificationTaskMapper;
import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.preference.mapper.NotificationUserPreferenceMapper;
import com.devops00.spectra.notification.request.javabean.entity.NotificationRequestEntity;
import com.devops00.spectra.notification.request.mapper.NotificationRequestMapper;
import com.devops00.spectra.notification.request.policy.NotificationPolicy;
import com.devops00.spectra.notification.template.mapper.NotificationTemplateMapper;
import com.devops00.spectra.notification.template.service.NotificationTemplateRenderer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Gateway 幂等和多收件人展开测试。 */
class NotificationGatewayImplTest {

    @Test
    void shouldExpandOneRequestIntoRecipientTasks() {
        var requestMapper = mock(NotificationRequestMapper.class);
        var taskMapper = mock(NotificationTaskMapper.class);
        var templateMapper = mock(NotificationTemplateMapper.class);
        var preferenceMapper = mock(NotificationUserPreferenceMapper.class);
        var directory = mock(NotificationRecipientDirectory.class);
        var protector = protector();
        var first = UUID.randomUUID();
        var second = UUID.randomUUID();
        when(requestMapper.selectOne(any())).thenReturn(null);
        when(requestMapper.insert(any(NotificationRequestEntity.class))).thenReturn(1);
        when(taskMapper.selectCount(any())).thenReturn(0L);
        when(taskMapper.insert(any(NotificationTaskEntity.class))).thenReturn(1);
        when(templateMapper.selectOne(any())).thenReturn(null);
        when(preferenceMapper.selectOne(any())).thenReturn(null);
        when(directory.resolve(any())).thenReturn(List.of(
                new NotificationRecipient(first, null, null, true, true),
                new NotificationRecipient(second, null, null, true, true)));

        var gateway = new NotificationGatewayImpl(requestMapper, taskMapper, templateMapper, preferenceMapper,
                new NotificationTemplateRenderer(), new NotificationPolicy(), new NotificationModuleProperties(true, "", ""),
                directory, protector, List.of());
        var receipt = gateway
                .enqueue(NotificationRequest.inApp("test:expand", com.devops00.spectra.common.notification.NotificationPurpose.SYSTEM_NOTICE,
                        List.of(first, second), "test", "标题", "正文", "TEST", "1", "TEST", null));

        assertEquals(2, receipt.taskCount());
        assertTrue(!receipt.idempotentReplay());
        verify(taskMapper, times(2)).insert(any(NotificationTaskEntity.class));
    }

    @Test
    void shouldReplayExistingRequestWithoutCreatingTasks() {
        var requestMapper = mock(NotificationRequestMapper.class);
        var taskMapper = mock(NotificationTaskMapper.class);
        var directory = mock(NotificationRecipientDirectory.class);
        var existing = new NotificationRequestEntity();
        existing.setId(UUID.randomUUID());
        existing.setStatus("DISPATCHING");
        when(requestMapper.selectOne(any())).thenReturn(existing);
        when(taskMapper.selectCount(any())).thenReturn(3L);

        var gateway = new NotificationGatewayImpl(requestMapper, taskMapper, mock(NotificationTemplateMapper.class),
                mock(NotificationUserPreferenceMapper.class), new NotificationTemplateRenderer(), new NotificationPolicy(),
                new NotificationModuleProperties(true, "", ""), directory, protector(), List.of());
        var receipt = gateway
                .enqueue(NotificationRequest.inApp("test:replay", com.devops00.spectra.common.notification.NotificationPurpose.SYSTEM_NOTICE,
                        List.of(UUID.randomUUID()), "test", "标题", "正文", "TEST", "2", "TEST", null));

        assertTrue(receipt.idempotentReplay());
        assertEquals(3, receipt.taskCount());
        verify(taskMapper, times(1)).selectCount(any());
        verify(taskMapper, times(0)).insert(any(NotificationTaskEntity.class));
    }

    @Test
    void shouldEncryptSensitiveParametersBeforePersistence() {
        var requestMapper = mock(NotificationRequestMapper.class);
        var taskMapper = mock(NotificationTaskMapper.class);
        var templateMapper = mock(NotificationTemplateMapper.class);
        var preferenceMapper = mock(NotificationUserPreferenceMapper.class);
        var directory = mock(NotificationRecipientDirectory.class);
        var recipientId = UUID.randomUUID();
        when(requestMapper.selectOne(any())).thenReturn(null);
        when(requestMapper.insert(any(NotificationRequestEntity.class))).thenReturn(1);
        when(taskMapper.selectCount(any())).thenReturn(0L);
        when(taskMapper.insert(any(NotificationTaskEntity.class))).thenReturn(1);
        when(templateMapper.selectOne(any())).thenReturn(null);
        when(preferenceMapper.selectOne(any())).thenReturn(null);
        when(directory.resolve(any())).thenReturn(List.of(
                new NotificationRecipient(recipientId, null, null, true, true)));

        var gateway = new NotificationGatewayImpl(requestMapper, taskMapper, templateMapper, preferenceMapper,
                new NotificationTemplateRenderer(), new NotificationPolicy(), new NotificationModuleProperties(true, "", ""),
                directory, protectorWithKey(), List.of());
        var request = new NotificationRequest(null, "test:sensitive", NotificationPurpose.SYSTEM_NOTICE,
                List.of(NotificationChannel.IN_APP), List.of(recipientId), List.of(), "login",
                java.util.Map.of("title", "登录通知", "content", "验证码 {{code}}"),
                java.util.Map.of("code", "123456"), "SECURITY", "login", "SECURITY", null, null, null, 0, "/login");

        var receipt = gateway.enqueue(request);

        assertEquals(1, receipt.taskCount());
        var requestCaptor = ArgumentCaptor.forClass(NotificationRequestEntity.class);
        verify(requestMapper).insert(requestCaptor.capture());
        assertFalse(requestCaptor.getValue().getSensitivePayload().contains("123456"));
        var taskCaptor = ArgumentCaptor.forClass(NotificationTaskEntity.class);
        verify(taskMapper).insert(taskCaptor.capture());
        assertEquals("安全通知", taskCaptor.getValue().getTitle());
        assertFalse(taskCaptor.getValue().getSensitivePayload().contains("123456"));
    }

    private NotificationPayloadProtector protector() {
        return new NotificationPayloadProtector(new NotificationModuleProperties(true, "", ""), new ObjectMapper());
    }

    private NotificationPayloadProtector protectorWithKey() {
        var key = Base64.getEncoder().encodeToString(new byte[32]);
        return new NotificationPayloadProtector(
                new NotificationModuleProperties(true, key, key), new ObjectMapper());
    }
}

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
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.mybatis.handler.UUIDTypeHandler;
import com.devops00.spectra.common.notification.*;
import com.devops00.spectra.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.notification.javabean.entity.NotificationRequestEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationTemplateEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationUserPreferenceEntity;
import com.devops00.spectra.notification.mapper.NotificationRequestMapper;
import com.devops00.spectra.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.notification.mapper.NotificationTemplateMapper;
import com.devops00.spectra.notification.mapper.NotificationUserPreferenceMapper;
import com.devops00.spectra.notification.properties.NotificationModuleProperties;
import com.devops00.spectra.notification.strategy.NotificationPolicy;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Gateway 幂等和多收件人展开测试。
 */
class NotificationGatewayImplTest {

    @BeforeAll
    static void initializeTableInfo() {
        var configuration = new MybatisConfiguration();
        configuration.getTypeHandlerRegistry().register(UUID.class, UUIDTypeHandler.class);
        var assistant = new MapperBuilderAssistant(configuration, "notification-gateway-test");
        TableInfoHelper.initTableInfo(assistant, NotificationRequestEntity.class);
        TableInfoHelper.initTableInfo(assistant, NotificationTaskEntity.class);
        TableInfoHelper.initTableInfo(assistant, NotificationTemplateEntity.class);
        TableInfoHelper.initTableInfo(assistant, NotificationUserPreferenceEntity.class);
    }

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
                new NotificationRecipient(first, null, null, true, true, null),
                new NotificationRecipient(second, null, null, true, true, null)));

        var gateway = new NotificationGatewayImpl(requestMapper, taskMapper, templateMapper, preferenceMapper,
                new NotificationTemplateRenderer(), new NotificationPolicy(), new NotificationModuleProperties(true, "", "", List.of()),
                directory, protector, List.of());
        var receipt = gateway
                .enqueue(NotificationRequest.inApp("test:expand", NotificationPurpose.SYSTEM_NOTICE,
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
                new NotificationModuleProperties(true, "", "", List.of()), directory, protector(), List.of());
        var receipt = gateway
                .enqueue(NotificationRequest.inApp("test:replay", NotificationPurpose.SYSTEM_NOTICE,
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
                new NotificationRecipient(recipientId, null, null, true, true, null)));

        var gateway = new NotificationGatewayImpl(requestMapper, taskMapper, templateMapper, preferenceMapper,
                new NotificationTemplateRenderer(), new NotificationPolicy(), new NotificationModuleProperties(true, "", "", List.of()),
                directory, protectorWithKey(), List.of());
        var request = new NotificationRequest(null, "test:sensitive", NotificationPurpose.SYSTEM_NOTICE,
                List.of(NotificationChannel.IN_APP), List.of(recipientId), List.of(), "login",
                Map.of("title", "登录通知", "content", "验证码 {{code}}"),
                Map.of("code", "123456"), "SECURITY", "login", "SECURITY", null, null, null, 0, "/login");

        var receipt = gateway.enqueue(request);

        assertEquals(1, receipt.taskCount());
        var requestCaptor = ArgumentCaptor.forClass(NotificationRequestEntity.class);
        verify(requestMapper).insert(requestCaptor.capture());
        assertFalse(requestCaptor.getValue().getSensitiveParametersCiphertext().contains("123456"));
        var taskCaptor = ArgumentCaptor.forClass(NotificationTaskEntity.class);
        verify(taskMapper).insert(taskCaptor.capture());
        assertEquals("安全通知", taskCaptor.getValue().getTitle());
        assertFalse(taskCaptor.getValue().getSensitiveParametersCiphertext().contains("123456"));
    }

    @Test
    void shouldRejectSensitiveTemplateParameterPassedAsOrdinary() {
        var requestMapper = mock(NotificationRequestMapper.class);
        var taskMapper = mock(NotificationTaskMapper.class);
        var templateMapper = mock(NotificationTemplateMapper.class);
        var preferenceMapper = mock(NotificationUserPreferenceMapper.class);
        var directory = mock(NotificationRecipientDirectory.class);
        var recipientId = UUID.randomUUID();
        var template = new NotificationTemplateEntity();
        template.setTemplateGroupCode("security.login-code");
        template.setChannel(NotificationChannel.IN_APP.name());
        template.setPurpose(NotificationPurpose.SYSTEM_NOTICE.name());
        template.setState("PUBLISHED");
        template.setVersionNo(1);
        template.setContentTemplate("验证码 {{code}}");
        template.setParameterSchema(Map.of("properties", Map.of(
                "code", Map.of("type", "string", "sensitive", true))));
        when(requestMapper.selectOne(any())).thenReturn(null);
        when(requestMapper.insert(any(NotificationRequestEntity.class))).thenReturn(1);
        when(taskMapper.selectCount(any())).thenReturn(0L);
        when(templateMapper.selectOne(any())).thenReturn(template);
        when(directory.resolve(any())).thenReturn(List.of(
                new NotificationRecipient(recipientId, null, null, true, true, null)));

        var gateway = gateway(requestMapper, taskMapper, templateMapper, preferenceMapper, directory);
        var request = new NotificationRequest(null, "test:sensitive-template", NotificationPurpose.SYSTEM_NOTICE,
                List.of(NotificationChannel.IN_APP), List.of(recipientId), List.of(), "security.login-code",
                Map.of("code", "123456"), Map.of(), "SECURITY", "login", "SECURITY", null,
                null, null, 0, null);

        assertThrows(DataSaveException.class, () -> gateway.enqueue(request));
        verify(taskMapper, never()).insert(any(NotificationTaskEntity.class));
    }

    @Test
    void shouldSkipOptionalChannelWhenUserDisabledIt() {
        var requestMapper = mock(NotificationRequestMapper.class);
        var taskMapper = mock(NotificationTaskMapper.class);
        var templateMapper = mock(NotificationTemplateMapper.class);
        var preferenceMapper = mock(NotificationUserPreferenceMapper.class);
        var directory = mock(NotificationRecipientDirectory.class);
        var recipientId = UUID.randomUUID();
        var preference = new NotificationUserPreferenceEntity();
        preference.setEnabled(false);
        preference.setDoNotDisturb(false);
        when(requestMapper.selectOne(any())).thenReturn(null);
        when(requestMapper.insert(any(NotificationRequestEntity.class))).thenReturn(1);
        when(taskMapper.selectCount(any())).thenReturn(0L);
        when(templateMapper.selectOne(any())).thenReturn(null);
        when(preferenceMapper.selectOne(any())).thenReturn(preference);
        when(directory.resolve(any())).thenReturn(List.of(
                new NotificationRecipient(recipientId, "13800138000", null, true, true, null)));

        var gateway = gateway(requestMapper, taskMapper, templateMapper, preferenceMapper, directory);
        var request = request(NotificationPurpose.SYSTEM_NOTICE, NotificationChannel.SMS, recipientId,
                "test:optional-disabled");

        var receipt = gateway.enqueue(request);

        assertEquals(0, receipt.taskCount());
        verify(taskMapper, never()).insert(any(NotificationTaskEntity.class));
    }

    @Test
    void shouldKeepMandatorySecurityChannelDespiteDisabledPreference() {
        var requestMapper = mock(NotificationRequestMapper.class);
        var taskMapper = mock(NotificationTaskMapper.class);
        var templateMapper = mock(NotificationTemplateMapper.class);
        var preferenceMapper = mock(NotificationUserPreferenceMapper.class);
        var directory = mock(NotificationRecipientDirectory.class);
        var recipientId = UUID.randomUUID();
        var preference = new NotificationUserPreferenceEntity();
        preference.setEnabled(false);
        preference.setDoNotDisturb(true);
        when(requestMapper.selectOne(any())).thenReturn(null);
        when(requestMapper.insert(any(NotificationRequestEntity.class))).thenReturn(1);
        when(taskMapper.selectCount(any())).thenReturn(0L);
        when(taskMapper.insert(any(NotificationTaskEntity.class))).thenReturn(1);
        when(templateMapper.selectOne(any())).thenReturn(null);
        when(preferenceMapper.selectOne(any())).thenReturn(preference);
        when(directory.resolve(any())).thenReturn(List.of(
                new NotificationRecipient(recipientId, null, null, true, true, null)));

        var gateway = gateway(requestMapper, taskMapper, templateMapper, preferenceMapper, directory);
        var request = request(NotificationPurpose.SECURITY_ALERT, NotificationChannel.IN_APP, recipientId,
                "test:security-alert");

        var receipt = gateway.enqueue(request);

        assertEquals(1, receipt.taskCount());
        verify(taskMapper).insert(any(NotificationTaskEntity.class));
        verify(preferenceMapper, never()).selectOne(any());
    }

    @Test
    void shouldRejectExternalAndUnregisteredLinks() {
        var gateway = gateway(mock(NotificationRequestMapper.class), mock(NotificationTaskMapper.class),
                mock(NotificationTemplateMapper.class), mock(NotificationUserPreferenceMapper.class),
                mock(NotificationRecipientDirectory.class));
        var recipientId = UUID.randomUUID();
        var external = NotificationRequest.inApp("test:external-link", NotificationPurpose.SYSTEM_NOTICE,
                List.of(recipientId), "test", "标题", "正文", "TEST", "1", "TEST", "https://evil.example/redirect");
        var unknown = NotificationRequest.inApp("test:unknown-link", NotificationPurpose.SYSTEM_NOTICE,
                List.of(recipientId), "test", "标题", "正文", "TEST", "2", "TEST", "/admin/secrets");

        assertThrows(DataSaveException.class, () -> gateway.enqueue(external));
        assertThrows(DataSaveException.class, () -> gateway.enqueue(unknown));
    }

    @Test
    void shouldSkipOptionalChannelDuringDoNotDisturb() {
        var requestMapper = mock(NotificationRequestMapper.class);
        var taskMapper = mock(NotificationTaskMapper.class);
        var templateMapper = mock(NotificationTemplateMapper.class);
        var preferenceMapper = mock(NotificationUserPreferenceMapper.class);
        var directory = mock(NotificationRecipientDirectory.class);
        var recipientId = UUID.randomUUID();
        var preference = new NotificationUserPreferenceEntity();
        preference.setEnabled(true);
        preference.setDoNotDisturb(true);
        when(requestMapper.selectOne(any())).thenReturn(null);
        when(requestMapper.insert(any(NotificationRequestEntity.class))).thenReturn(1);
        when(taskMapper.selectCount(any())).thenReturn(0L);
        when(templateMapper.selectOne(any())).thenReturn(null);
        when(preferenceMapper.selectOne(any())).thenReturn(preference);
        when(directory.resolve(any())).thenReturn(List.of(
                new NotificationRecipient(recipientId, null, "user@example.com", true, true, null)));

        var gateway = gateway(requestMapper, taskMapper, templateMapper, preferenceMapper, directory);
        var request = request(NotificationPurpose.SYSTEM_NOTICE, NotificationChannel.EMAIL, recipientId,
                "test:do-not-disturb");

        var receipt = gateway.enqueue(request);

        assertEquals(0, receipt.taskCount());
        verify(taskMapper, never()).insert(any(NotificationTaskEntity.class));
    }

    @Test
    void shouldSkipOptionalChannelInsideConfiguredDoNotDisturbWindow() {
        var requestMapper = mock(NotificationRequestMapper.class);
        var taskMapper = mock(NotificationTaskMapper.class);
        var templateMapper = mock(NotificationTemplateMapper.class);
        var preferenceMapper = mock(NotificationUserPreferenceMapper.class);
        var directory = mock(NotificationRecipientDirectory.class);
        var recipientId = UUID.randomUUID();
        var preference = new NotificationUserPreferenceEntity();
        preference.setEnabled(true);
        preference.setDoNotDisturb(true);
        var now = Instant.now();
        preference.setDoNotDisturbStart(now.minusSeconds(60));
        preference.setDoNotDisturbEnd(now.plusSeconds(60));
        when(requestMapper.selectOne(any())).thenReturn(null);
        when(requestMapper.insert(any(NotificationRequestEntity.class))).thenReturn(1);
        when(taskMapper.selectCount(any())).thenReturn(0L);
        when(templateMapper.selectOne(any())).thenReturn(null);
        when(preferenceMapper.selectOne(any())).thenReturn(preference);
        when(directory.resolve(any())).thenReturn(List.of(
                new NotificationRecipient(recipientId, null, "user@example.com", true, true, "UTC")));

        var gateway = gateway(requestMapper, taskMapper, templateMapper, preferenceMapper, directory);
        var request = request(NotificationPurpose.SYSTEM_NOTICE, NotificationChannel.EMAIL, recipientId,
                "test:do-not-disturb-window");

        var receipt = gateway.enqueue(request);

        assertEquals(0, receipt.taskCount());
        verify(taskMapper, never()).insert(any(NotificationTaskEntity.class));
    }

    private NotificationGatewayImpl gateway(NotificationRequestMapper requestMapper,
                                            NotificationTaskMapper taskMapper,
                                            NotificationTemplateMapper templateMapper,
                                            NotificationUserPreferenceMapper preferenceMapper,
                                            NotificationRecipientDirectory directory) {
        return new NotificationGatewayImpl(requestMapper, taskMapper, templateMapper, preferenceMapper,
                new NotificationTemplateRenderer(), new NotificationPolicy(),
                new NotificationModuleProperties(true, "", "", List.of()), directory, protector(), List.of());
    }

    private NotificationRequest request(NotificationPurpose purpose, NotificationChannel channel, UUID recipientId,
                                        String idempotencyKey) {
        return new NotificationRequest(null, idempotencyKey, purpose, List.of(channel), List.of(recipientId),
                List.of(), "test", Map.of("title", "通知", "content", "正文"), Map.of(),
                "SYSTEM", idempotencyKey, "SYSTEM", null, null, null, 0, null);
    }

    private NotificationPayloadProtector protector() {
        return new NotificationPayloadProtector(new NotificationModuleProperties(true, "", "", List.of()), new ObjectMapper());
    }

    private NotificationPayloadProtector protectorWithKey() {
        var key = Base64.getEncoder().encodeToString(new byte[32]);
        return new NotificationPayloadProtector(
                new NotificationModuleProperties(true, key, key, List.of()), new ObjectMapper());
    }
}

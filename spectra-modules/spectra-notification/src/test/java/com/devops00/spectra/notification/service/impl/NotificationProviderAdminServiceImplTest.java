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

import com.devops00.spectra.common.config.SystemConfigValueProvider;
import com.devops00.spectra.common.config.SystemConfigValueWriter;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.notification.javabean.from.NotificationProviderSaveFrom;
import com.devops00.spectra.notification.properties.NotificationModuleProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Provider 配置服务测试；验证全局配置、Secret 密文和状态 fail-closed 规则。
 */
class NotificationProviderAdminServiceImplTest {

    private final Map<String, String> values = new HashMap<>();
    private NotificationProviderAdminServiceImpl service;

    @BeforeEach
    void setUp() {
        var provider = mock(SystemConfigValueProvider.class);
        when(provider.find(ArgumentMatchers.anyString())).thenAnswer(invocation -> Optional.ofNullable(values.get(invocation.getArgument(0))));
        var writer = mock(SystemConfigValueWriter.class);
        doAnswer(invocation -> {
            var key = invocation.getArgument(0, String.class);
            var value = invocation.getArgument(1, String.class);
            if (value == null) {
                throw new AssertionError("系统配置不允许写入 null 值");
            } else if (value.isBlank()) {
                values.remove(key);
            } else {
                values.put(key, value);
            }
            return null;
        }).when(writer)
                .upsert(ArgumentMatchers.anyString(), ArgumentMatchers.any(), ArgumentMatchers.any(),
                        ArgumentMatchers.anyString());
        var key = java.util.Base64.getEncoder().encodeToString(new byte[32]);
        var protector = new NotificationPayloadProtector(
                new NotificationModuleProperties(true, key, key, java.util.List.of()), new ObjectMapper());
        service = new NotificationProviderAdminServiceImpl(provider, protector, new ObjectMapper());
        service.setValueWriter(writer);
    }

    @Test
    void shouldReturnSafeDefaultsForExternalChannels() {
        var sms = service.get(NotificationChannel.SMS);
        var inApp = service.get(NotificationChannel.IN_APP);

        assertEquals("NOT_CONFIGURED", sms.getState());
        assertFalse(sms.isSecretConfigured());
        assertEquals("HEALTHY", inApp.getState());
        assertEquals("IN_APP", inApp.getProviderType());
    }

    @Test
    void shouldEncryptSecretAndNeverExposeIt() {
        var params = new NotificationProviderSaveFrom();
        params.setProviderType("HTTP_JSON");
        params.setEnabled(true);
        params.setEndpoint("https://provider.example.test/send");
        params.setSecret("test-secret-value");

        var result = service.modify(NotificationChannel.SMS, params);

        assertEquals("UNHEALTHY", result.getState());
        assertTrue(result.isSecretConfigured());
        assertNotNull(result.getSecretKeyId());
        assertNotEquals("test-secret-value", values.get("notification.provider.sms.secret"));
        assertFalse(values.get("notification.provider.sms.secret").contains("test-secret-value"));
        assertFalse(values.get("notification.provider.sms").contains("test-secret-value"));
        assertFalse(result.toString().contains("test-secret-value"));
    }

    @Test
    void shouldSaveAliyunSmsConfigurationWithProviderDefaults() {
        var params = new NotificationProviderSaveFrom();
        params.setProviderType("ALIYUN_SMS");
        params.setEnabled(true);
        params.setCredentialId("LTAI_TEST");
        params.setSignName("Spectra");
        params.setTemplateCode("SMS_123456");
        params.setSecret("aliyun-secret");

        var result = service.modify(NotificationChannel.SMS, params);

        assertEquals("ALIYUN_SMS", result.getProviderType());
        assertEquals("dysmsapi.aliyuncs.com", result.getEndpoint());
        assertEquals("cn-hangzhou", result.getRegion());
        assertEquals("LTAI_TEST", result.getCredentialId());
        assertEquals("UNHEALTHY", result.getState());
        assertTrue(result.isSecretConfigured());
    }

    @Test
    void shouldSaveMockProviderWithoutThirdPartySecret() {
        var params = new NotificationProviderSaveFrom();
        params.setProviderType("MOCK");
        params.setEnabled(true);

        var result = service.modify(NotificationChannel.EMAIL, params);

        assertEquals("MOCK", result.getProviderType());
        assertEquals("", result.getEndpoint());
        assertEquals(0, result.getPort());
        assertEquals(0, result.getTimeoutMs());
        assertEquals(0, result.getRateLimitPerSecond());
        assertEquals(1, result.getMaxAttempts());
        assertEquals("UNHEALTHY", result.getState());
        assertEquals("HEALTH_CHECK_REQUIRED", result.getReason());
        assertFalse(result.isSecretConfigured());
    }

    @Test
    void shouldResolveSavedMockProviderForRuntime() {
        var params = new NotificationProviderSaveFrom();
        params.setProviderType("MOCK");
        params.setEnabled(true);
        service.modify(NotificationChannel.SMS, params);

        var configuration = service.resolve(NotificationChannel.SMS);

        assertEquals("MOCK", configuration.providerType());
        assertTrue(configuration.enabled());
        assertEquals(NotificationChannel.SMS, configuration.channel());
    }

    @Test
    void shouldDiscardOldSecretWhenProviderTypeChanges() {
        var first = new NotificationProviderSaveFrom();
        first.setProviderType("HTTP_JSON");
        first.setEndpoint("https://provider.example.test/send");
        first.setSecret("old-secret");
        service.modify(NotificationChannel.SMS, first);

        var second = new NotificationProviderSaveFrom();
        second.setProviderType("MOCK");
        second.setEnabled(true);
        var result = service.modify(NotificationChannel.SMS, second);

        assertEquals("MOCK", result.getProviderType());
        assertFalse(result.isSecretConfigured());
        assertFalse(values.containsKey("notification.provider.sms.secret"));
    }

    @Test
    void shouldRejectBothSmtpSecurityModes() {
        var params = new NotificationProviderSaveFrom();
        params.setProviderType("SMTP");
        params.setEndpoint("smtp.example.com");
        params.setCredentialId("mailer@example.com");
        params.setSenderAddress("no-reply@example.com");
        params.setSslEnabled(true);
        params.setStarttlsEnabled(true);

        assertThrows(com.devops00.spectra.common.exception.DataSaveException.class,
                () -> service.modify(NotificationChannel.EMAIL, params));
    }

    @Test
    void shouldRejectSmtpWithoutSenderAddress() {
        var params = new NotificationProviderSaveFrom();
        params.setProviderType("SMTP");
        params.setEndpoint("smtp.example.com");
        params.setCredentialId("mailer@example.com");
        params.setSecret("smtp-secret");

        assertThrows(com.devops00.spectra.common.exception.DataSaveException.class,
                () -> service.modify(NotificationChannel.EMAIL, params));
    }

    @Test
    void shouldBlockHttpProviderWithoutSecret() {
        var params = new NotificationProviderSaveFrom();
        params.setProviderType("HTTP_JSON");
        params.setEnabled(true);
        params.setEndpoint("https://provider.example.test/send");

        var result = service.modify(NotificationChannel.EMAIL, params);

        assertEquals("BLOCKED", result.getState());
        assertEquals("SECRET_NOT_CONFIGURED", result.getReason());
    }

    @Test
    void shouldRejectInvalidHttpEndpoint() {
        var params = new NotificationProviderSaveFrom();
        params.setProviderType("HTTP_JSON");
        params.setEndpoint("file:///provider");

        assertThrows(com.devops00.spectra.common.exception.DataSaveException.class,
                () -> service.modify(NotificationChannel.SMS, params));
    }

    @Test
    void shouldClearExistingSecretWithoutReturningIt() {
        var params = new NotificationProviderSaveFrom();
        params.setProviderType("MOCK");
        params.setEnabled(true);
        params.setSecret("mock-secret");
        service.modify(NotificationChannel.SMS, params);

        var clear = new NotificationProviderSaveFrom();
        clear.setProviderType("MOCK");
        clear.setEnabled(false);
        clear.setClearSecret(true);
        var result = service.modify(NotificationChannel.SMS, clear);

        assertFalse(result.isSecretConfigured());
        assertEquals("DISABLED", result.getState());
        assertFalse(values.containsKey("notification.provider.sms.secret"));
    }
}

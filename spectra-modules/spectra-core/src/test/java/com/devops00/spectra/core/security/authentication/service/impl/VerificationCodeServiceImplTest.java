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

package com.devops00.spectra.core.security.authentication.service.impl;

import com.devops00.spectra.core.common.constant.RedisCacheKey;
import com.devops00.spectra.common.port.security.SecurityVerificationCodeStore;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationReceipt;
import com.devops00.spectra.common.notification.NotificationSendRequest;
import com.devops00.spectra.common.notification.NotificationService;
import com.devops00.spectra.framework.configure.security.properties.SecurityProperties;
import com.devops00.spectra.common.security.crypto.VerificationCodeDigest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** 验证码摘要、过期和发送失败补偿测试。 */
class VerificationCodeServiceImplTest {

    @Test
    void shouldStoreDigestAndSendSensitiveRequest() throws Exception {
        var notificationService = mock(NotificationService.class);
        var verificationCodeStore = mock(SecurityVerificationCodeStore.class);
        when(notificationService.send(any(NotificationSendRequest.class)))
                .thenReturn(new NotificationReceipt(UUID.randomUUID(), "ACCEPTED", 1, false));
        when(verificationCodeStore.saveIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);
        var properties = new SecurityProperties();
        properties.setVerificationCodeHmacKey("test-verification-hmac-key");
        var service = new VerificationCodeServiceImpl(notificationService, verificationCodeStore, properties);

        service.sendSmsCode("13800138000");

        var requestCaptor = org.mockito.ArgumentCaptor.forClass(NotificationSendRequest.class);
        verify(notificationService).send(requestCaptor.capture());
        var request = requestCaptor.getValue();
        var code = request.sensitiveParameters().get("code").toString();
        assertTrue(code.matches("\\d{6}"));
        assertTrue(request.idempotencyKey().startsWith("security:login-code:SMS:13800138000:"));
        var key = RedisCacheKey.LOGIN_SMS_CODE + "13800138000";
        verify(verificationCodeStore).saveIfAbsent(eq(key), eq(VerificationCodeDigest.digest(code,
                "test-verification-hmac-key")), eq(Duration.ofSeconds(300)));
    }

    @Test
    void shouldNotCreateAnotherRequestWhenCodeWindowAlreadyExists() {
        var notificationService = mock(NotificationService.class);
        var verificationCodeStore = mock(SecurityVerificationCodeStore.class);
        when(verificationCodeStore.saveIfAbsent(any(), any(), any(Duration.class))).thenReturn(false);
        var properties = new SecurityProperties();
        properties.setVerificationCodeHmacKey("test-verification-hmac-key");
        var service = new VerificationCodeServiceImpl(notificationService, verificationCodeStore, properties);

        service.sendSmsCode("13800138000");

        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldUseDedicatedPurposeAndKeyForBindingCode() {
        var notificationService = mock(NotificationService.class);
        var verificationCodeStore = mock(SecurityVerificationCodeStore.class);
        when(notificationService.send(any(NotificationSendRequest.class)))
                .thenReturn(new NotificationReceipt(UUID.randomUUID(), "ACCEPTED", 1, false));
        when(verificationCodeStore.saveIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);
        var properties = new SecurityProperties();
        properties.setVerificationCodeHmacKey("test-verification-hmac-key");
        var service = new VerificationCodeServiceImpl(notificationService, verificationCodeStore, properties);

        service.sendBindingSmsCode("13800138000");

        var requestCaptor = org.mockito.ArgumentCaptor.forClass(NotificationSendRequest.class);
        verify(notificationService).send(requestCaptor.capture());
        var request = requestCaptor.getValue();
        var code = request.sensitiveParameters().get("code").toString();
        assertTrue(request.purpose() == NotificationPurpose.BIND_PHONE_CODE);
        assertTrue(request.idempotencyKey().startsWith("security:bind-phone-code:SMS:13800138000:"));
        verify(verificationCodeStore).saveIfAbsent(eq(RedisCacheKey.BIND_PHONE_CODE + "13800138000"),
                eq(VerificationCodeDigest.digest(code, "test-verification-hmac-key")), eq(Duration.ofSeconds(300)));
    }

    @Test
    void shouldDeleteDigestWhenGatewayEnqueueFails() {
        var notificationService = mock(NotificationService.class);
        var verificationCodeStore = mock(SecurityVerificationCodeStore.class);
        when(notificationService.send(any(NotificationSendRequest.class))).thenThrow(new IllegalStateException("mock failure"));
        when(verificationCodeStore.saveIfAbsent(any(), any(), any(Duration.class))).thenReturn(true);
        var properties = new SecurityProperties();
        properties.setVerificationCodeHmacKey("test-verification-hmac-key");
        var service = new VerificationCodeServiceImpl(notificationService, verificationCodeStore, properties);

        assertThrows(RuntimeException.class, () -> service.sendEmailCode("user@example.com"));

        verify(verificationCodeStore).delete(RedisCacheKey.LOGIN_EMAIL_CODE + "user@example.com");
    }

    @Test
    void shouldRejectMissingHmacKeyBeforeWritingRedis() {
        var notificationService = mock(NotificationService.class);
        var verificationCodeStore = mock(SecurityVerificationCodeStore.class);
        var service = new VerificationCodeServiceImpl(notificationService, verificationCodeStore, new SecurityProperties());

        assertThrows(RuntimeException.class, () -> service.sendSmsCode("13800138000"));

        verifyNoInteractions(verificationCodeStore);
        verifyNoInteractions(notificationService);
    }

    @Test
    void shouldRequireNotificationServiceAsConstructorDependency() throws NoSuchMethodException {
        var constructor = VerificationCodeServiceImpl.class.getConstructor(
                NotificationService.class, SecurityVerificationCodeStore.class, SecurityProperties.class);

        assertEquals(NotificationService.class, constructor.getParameterTypes()[0]);
    }
}

/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.devops00.spectra.core.security.authentication.service.impl;

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.common.exception.SpectraException;
import com.devops00.spectra.common.exception.KaptchaNotMatchException;
import com.devops00.spectra.common.notification.NotificationReceipt;
import com.devops00.spectra.common.notification.NotificationSendRequest;
import com.devops00.spectra.common.notification.NotificationService;
import com.devops00.spectra.common.port.security.SecurityVerificationCodeStore;
import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.core.common.constant.RedisCacheKey;
import com.devops00.spectra.core.security.authentication.crypto.VerificationCodeDigest;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.core.security.authentication.service.UserContactService;
import com.devops00.spectra.core.security.secret.service.SecretRuntimeService;
import com.devops00.spectra.core.security.root.LastEffectiveDevOpsGuard;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 绑定验证码的用户范围及身份写入边界。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/11
 */
class BindingVerificationCodeTest {
    private static final String SECRET = "synthetic-binding-secret";
    private final SecurityVerificationCodeStore codes = mock(SecurityVerificationCodeStore.class);
    private final SecretRuntimeService secrets = mock(SecretRuntimeService.class);
    private final SecurityProperties properties = new SecurityProperties();
    private final AuthenticationIdentityService identities = mock(AuthenticationIdentityService.class);
    private final UserContactService contacts = mock(UserContactService.class);

    @Test
    void sendingPhoneCodeStoresUserBoundDigestAndNotifiesTarget() {
        var notifications = mock(NotificationService.class);
        var userId = UUID.randomUUID();
        var address = "13800000001";
        when(secrets.requireActiveValue("security.verification-code-hmac")).thenReturn(SECRET);
        when(codes.saveIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(notifications.send(any())).thenReturn(new NotificationReceipt(UUID.randomUUID(), "ACCEPTED", 1, false));

        new VerificationCodeServiceImpl(notifications, codes, properties, secrets).sendBindingSmsCode(userId, address);

        var storedValue = ArgumentCaptor.forClass(String.class);
        var request = ArgumentCaptor.forClass(NotificationSendRequest.class);
        verify(codes).saveIfAbsent(eq(RedisCacheKey.BIND_PHONE_CODE + address), storedValue.capture(),
                eq(Duration.ofSeconds(properties.getVerificationCodeExpire())));
        verify(notifications).send(request.capture());
        var code = (String) request.getValue().sensitiveParameters().get("code");
        assertEquals(userId + ":" + VerificationCodeDigest.digest(code, SECRET), storedValue.getValue());
        assertTrue(request.getValue().idempotencyKey().contains(userId.toString()));
    }

    @Test
    void sendingEmailCodeAlsoBindsUserAndRedisFailureDoesNotSend() {
        var notifications = mock(NotificationService.class);
        var userId = UUID.randomUUID();
        when(secrets.requireActiveValue("security.verification-code-hmac")).thenReturn(SECRET);
        when(codes.saveIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenThrow(new SecurityRedisUnavailableException("synthetic failure", null));
        var service = new VerificationCodeServiceImpl(notifications, codes, properties, secrets);

        assertThrows(SecurityRedisUnavailableException.class,
                () -> service.sendBindingEmailCode(userId, "user@example.test"));
        verifyNoInteractions(notifications);
    }

    @Test
    void emailCodeCanBeReissuedWithFreshNotificationKeyAfterFailedDelivery() {
        var notifications = mock(NotificationService.class);
        var userId = UUID.randomUUID();
        var address = "user@example.test";
        when(secrets.requireActiveValue("security.verification-code-hmac")).thenReturn(SECRET);
        when(codes.saveIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(notifications.send(any())).thenReturn(new NotificationReceipt(UUID.randomUUID(), "EMPTY", 0, false),
                new NotificationReceipt(UUID.randomUUID(), "ACCEPTED", 1, false));
        var service = new VerificationCodeServiceImpl(notifications, codes, properties, secrets);

        assertThrows(SpectraException.class, () -> service.sendBindingEmailCode(userId, address));
        service.sendBindingEmailCode(userId, address);

        var storedValues = ArgumentCaptor.forClass(String.class);
        var requests = ArgumentCaptor.forClass(NotificationSendRequest.class);
        verify(codes, org.mockito.Mockito.times(2)).saveIfAbsent(eq(RedisCacheKey.BIND_EMAIL_CODE + address),
                storedValues.capture(), any(Duration.class));
        verify(codes).compareAndDelete(RedisCacheKey.BIND_EMAIL_CODE + address,
                storedValues.getAllValues().getFirst());
        verify(notifications, org.mockito.Mockito.times(2)).send(requests.capture());
        assertNotEquals(requests.getAllValues().get(0).idempotencyKey(),
                requests.getAllValues().get(1).idempotencyKey());
        var deliveredCode = (String) requests.getAllValues().get(1).sensitiveParameters().get("code");
        assertEquals(userId + ":" + VerificationCodeDigest.digest(deliveredCode, SECRET),
                storedValues.getAllValues().get(1));
        assertTrue(requests.getAllValues().get(1).businessId().length() <= 100);
        assertTrue(requests.getAllValues().get(1).idempotencyKey().length() <= 200);
    }

    @Test
    void matchedCodeWritesIdentityAndContactForSameUser() {
        var userId = UUID.randomUUID();
        var address = "user@example.test";
        var code = "123456";
        when(secrets.requireActiveValue("security.verification-code-hmac")).thenReturn(SECRET);
        when(codes.consumeWithinAttemptBudget(anyString(), anyString(), anyString(), eq(5)))
                .thenReturn(SecurityVerificationCodeStore.VerificationResult.MATCHED);

        bindingService().bindEmail(userId, address, code);

        var key = RedisCacheKey.BIND_EMAIL_CODE + address;
        verify(codes).consumeWithinAttemptBudget(key, key + ":attempts",
                userId + ":" + VerificationCodeDigest.digest(code, SECRET), 5);
        verify(identities).createIdentity(userId, "EMAIL", address);
        verify(contacts).upsertVerified(userId, UserContactService.EMAIL, address);
    }

    @Test
    void invalidAndExhaustedCodesNeverWriteIdentity() {
        var userId = UUID.randomUUID();
        when(secrets.requireActiveValue("security.verification-code-hmac")).thenReturn(SECRET);
        when(codes.consumeWithinAttemptBudget(anyString(), anyString(), anyString(), eq(5)))
                .thenReturn(SecurityVerificationCodeStore.VerificationResult.INVALID,
                        SecurityVerificationCodeStore.VerificationResult.EXHAUSTED);

        assertThrows(KaptchaNotMatchException.class, () -> bindingService().bindPhone(userId, "13800000001", "000000"));
        assertThrows(KaptchaNotMatchException.class, () -> bindingService().bindPhone(userId, "13800000001", "000000"));
        verifyNoInteractions(identities, contacts);
    }

    private AuthenticationIdentityBindingServiceImpl bindingService() {
        return new AuthenticationIdentityBindingServiceImpl(codes, properties, secrets, identities, contacts,
                mock(LastEffectiveDevOpsGuard.class), mock(SecuritySessionRevocationPort.class));
    }
}

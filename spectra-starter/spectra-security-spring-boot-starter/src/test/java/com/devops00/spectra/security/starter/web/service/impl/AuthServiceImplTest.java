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

package com.devops00.spectra.security.starter.web.service.impl;

import com.devops00.spectra.common.constant.RedisCacheKey;
import com.devops00.spectra.common.notification.*;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.base.util.VerificationCodeDigest;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 验证码摘要、过期和发送失败补偿测试。
 */
class AuthServiceImplTest {

    @Test
    void shouldStoreDigestAndSendSensitiveRequest() throws Exception {
        var gateway = mock(NotificationGateway.class);
        var redisTemplate = mock(RedisTemplate.class);
        var valueOperations = mock(ValueOperations.class);
        when(gateway.availability(NotificationChannel.SMS))
                .thenReturn(new NotificationChannelAvailability(NotificationChannel.SMS, true, "AVAILABLE"));
        when(gateway.enqueue(any(NotificationRequest.class)))
                .thenReturn(new NotificationReceipt(UUID.randomUUID(), "ACCEPTED", 1, false));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        var properties = new SecurityProperties();
        properties.setVerificationCodeHmacKey("test-verification-hmac-key");
        var service = new AuthServiceImpl(gateway, redisTemplate, properties);

        service.sendSmsCode("13800138000");

        var requestCaptor = org.mockito.ArgumentCaptor.forClass(NotificationRequest.class);
        verify(gateway).enqueue(requestCaptor.capture());
        var request = requestCaptor.getValue();
        var code = request.sensitiveParameters().get("code").toString();
        assertTrue(code.matches("\\d{6}"));
        assertEquals("13800138000", request.directAddresses().getFirst().address());
        assertTrue(request.idempotencyKey().startsWith("security:login-code:SMS:13800138000:"));
        var key = RedisCacheKey.SMS_CODE + "13800138000";
        verify(valueOperations).setIfAbsent(eq(key), eq(VerificationCodeDigest.digest(code, "test-verification-hmac-key")),
                eq(300L), eq(TimeUnit.SECONDS));
    }

    @Test
    void shouldNotCreateAnotherRequestWhenCodeWindowAlreadyExists() {
        var gateway = mock(NotificationGateway.class);
        var redisTemplate = mock(RedisTemplate.class);
        var valueOperations = mock(ValueOperations.class);
        when(gateway.availability(NotificationChannel.SMS))
                .thenReturn(new NotificationChannelAvailability(NotificationChannel.SMS, true, "AVAILABLE"));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), anyLong(), any(TimeUnit.class))).thenReturn(false);
        var properties = new SecurityProperties();
        properties.setVerificationCodeHmacKey("test-verification-hmac-key");
        var service = new AuthServiceImpl(gateway, redisTemplate, properties);

        service.sendSmsCode("13800138000");

        verify(gateway, never()).enqueue(any(NotificationRequest.class));
    }

    @Test
    void shouldDeleteDigestWhenGatewayEnqueueFails() {
        var gateway = mock(NotificationGateway.class);
        var redisTemplate = mock(RedisTemplate.class);
        var valueOperations = mock(ValueOperations.class);
        when(gateway.availability(NotificationChannel.EMAIL))
                .thenReturn(new NotificationChannelAvailability(NotificationChannel.EMAIL, true, "AVAILABLE"));
        when(gateway.enqueue(any(NotificationRequest.class))).thenThrow(new IllegalStateException("mock failure"));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(any(), any(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        var properties = new SecurityProperties();
        properties.setVerificationCodeHmacKey("test-verification-hmac-key");
        var service = new AuthServiceImpl(gateway, redisTemplate, properties);

        assertThrows(RuntimeException.class, () -> service.sendEmailCode("user@example.com"));

        verify(redisTemplate).delete(RedisCacheKey.EMAIL_CODE + "user@example.com");
    }

    @Test
    void shouldRejectUnavailableChannelBeforeWritingRedis() {
        var gateway = mock(NotificationGateway.class);
        var redisTemplate = mock(RedisTemplate.class);
        when(gateway.availability(NotificationChannel.SMS))
                .thenReturn(new NotificationChannelAvailability(NotificationChannel.SMS, false, "CHANNEL_NOT_CONFIGURED"));
        var service = new AuthServiceImpl(gateway, redisTemplate, new SecurityProperties());

        assertThrows(RuntimeException.class, () -> service.sendSmsCode("13800138000"));

        verifyNoInteractions(redisTemplate);
    }

    @Test
    void shouldRejectMissingHmacKeyBeforeWritingRedis() {
        var gateway = mock(NotificationGateway.class);
        var redisTemplate = mock(RedisTemplate.class);
        when(gateway.availability(NotificationChannel.SMS))
                .thenReturn(new NotificationChannelAvailability(NotificationChannel.SMS, true, "AVAILABLE"));
        var service = new AuthServiceImpl(gateway, redisTemplate, new SecurityProperties());

        assertThrows(RuntimeException.class, () -> service.sendSmsCode("13800138000"));

        verifyNoInteractions(redisTemplate);
        verify(gateway, never()).enqueue(any(NotificationRequest.class));
    }
}

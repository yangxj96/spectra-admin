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

import com.devops00.spectra.common.constant.RedisCacheKey;
import com.devops00.spectra.common.exception.KaptchaNotMatchException;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.core.security.authentication.service.PasswordCredentialService;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.base.util.VerificationCodeDigest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 短信验证码 HMAC 校验、尝试次数和清理回归测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
class LoginSmsProviderTest {

    private static final String PHONE = "13800138000";

    private static final String HMAC_KEY = "test-verification-hmac-key";

    private RedisTemplate<String, Object> redisTemplate;

    private ValueOperations<String, Object> valueOperations;

    private LoginSmsProvider provider;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        var properties = new SecurityProperties();
        properties.setVerificationCodeHmacKey(HMAC_KEY);
        provider = new LoginSmsProvider(redisTemplate, mock(UserService.class), mock(AuthenticationIdentityService.class),
                mock(PasswordCredentialService.class), mock(SecurityUserHelper.class), properties);
    }

    @Test
    void shouldConsumeDigestAtomicallyAndStartAttemptWindow() {
        var key = RedisCacheKey.LOGIN_SMS_CODE + PHONE;
        when(valueOperations.increment(RedisCacheKey.LOGIN_SMS_CODE_ATTEMPTS + PHONE)).thenReturn(1L);
        when(redisTemplate.execute(any(RedisScript.class), eq(List.of(key)), eq(VerificationCodeDigest.digest("123456", HMAC_KEY))))
                .thenReturn(1L);

        provider.kaptchaValidate(PHONE, "123456");

        verify(redisTemplate).expire(RedisCacheKey.LOGIN_SMS_CODE_ATTEMPTS + PHONE, 300L, TimeUnit.SECONDS);
        verify(redisTemplate).execute(any(RedisScript.class), eq(List.of(key)), eq(VerificationCodeDigest.digest("123456", HMAC_KEY)));
    }

    @Test
    void shouldRejectWhenAttemptsExceedLimitBeforeReadingCode() {
        when(valueOperations.increment(RedisCacheKey.LOGIN_SMS_CODE_ATTEMPTS + PHONE)).thenReturn(6L);

        assertThrows(KaptchaNotMatchException.class, () -> provider.kaptchaValidate(PHONE, "123456"));

        verify(valueOperations, never()).get(any());
    }

    @Test
    void shouldRejectDigestMismatchWithoutReadingOrDeletingInApplicationCode() {
        var key = RedisCacheKey.LOGIN_SMS_CODE + PHONE;
        when(valueOperations.increment(RedisCacheKey.LOGIN_SMS_CODE_ATTEMPTS + PHONE)).thenReturn(1L);
        when(redisTemplate.execute(any(RedisScript.class), eq(List.of(key)), eq(VerificationCodeDigest.digest("123456", HMAC_KEY))))
                .thenReturn(0L);

        assertThrows(KaptchaNotMatchException.class, () -> provider.kaptchaValidate(PHONE, "123456"));

        verify(redisTemplate).execute(any(RedisScript.class), eq(List.of(key)), eq(VerificationCodeDigest.digest("123456", HMAC_KEY)));
        verify(valueOperations, never()).get(any());
        verify(redisTemplate, never()).delete(any(String.class));
    }
}

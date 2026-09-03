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

package com.devops00.spectra.core.security.authentication.provider;

import com.devops00.spectra.core.common.constant.RedisCacheKey;
import com.devops00.spectra.common.port.security.SecurityVerificationAttemptStore;
import com.devops00.spectra.common.port.security.SecurityVerificationCodeStore;
import com.devops00.spectra.common.exception.KaptchaNotMatchException;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.core.security.authentication.service.PasswordCredentialService;
import com.devops00.spectra.core.security.authentication.service.impl.SecurityUserHelper;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.framework.configure.security.properties.SecurityProperties;
import com.devops00.spectra.common.security.crypto.VerificationCodeDigest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private SecurityVerificationCodeStore verificationCodeStore;

    private SecurityVerificationAttemptStore verificationAttemptStore;

    private LoginSmsProvider provider;

    @BeforeEach
    void setUp() {
        verificationCodeStore = mock(SecurityVerificationCodeStore.class);
        verificationAttemptStore = mock(SecurityVerificationAttemptStore.class);
        var properties = new SecurityProperties();
        properties.setVerificationCodeHmacKey(HMAC_KEY);
        provider = new LoginSmsProvider(verificationCodeStore, verificationAttemptStore, mock(UserService.class),
                mock(AuthenticationIdentityService.class), mock(PasswordCredentialService.class),
                mock(SecurityUserHelper.class), properties);
    }

    @Test
    void shouldConsumeDigestAtomicallyAndStartAttemptWindow() {
        var key = RedisCacheKey.LOGIN_SMS_CODE + PHONE;
        when(verificationAttemptStore.increment(eq(RedisCacheKey.LOGIN_SMS_CODE_ATTEMPTS + PHONE), any(Duration.class)))
                .thenReturn(1L);
        when(verificationCodeStore.compareAndDelete(key, VerificationCodeDigest.digest("123456", HMAC_KEY)))
                .thenReturn(true);

        provider.kaptchaValidate(PHONE, "123456");

        verify(verificationAttemptStore).increment(eq(RedisCacheKey.LOGIN_SMS_CODE_ATTEMPTS + PHONE), any(Duration.class));
        verify(verificationCodeStore).compareAndDelete(key, VerificationCodeDigest.digest("123456", HMAC_KEY));
    }

    @Test
    void shouldRejectWhenAttemptsExceedLimitBeforeReadingCode() {
        when(verificationAttemptStore.increment(eq(RedisCacheKey.LOGIN_SMS_CODE_ATTEMPTS + PHONE), any(Duration.class)))
                .thenReturn(6L);

        assertThrows(KaptchaNotMatchException.class, () -> provider.kaptchaValidate(PHONE, "123456"));

        verify(verificationCodeStore, never()).compareAndDelete(any(), any());
    }

    @Test
    void shouldRejectDigestMismatchWithoutReadingOrDeletingInApplicationCode() {
        var key = RedisCacheKey.LOGIN_SMS_CODE + PHONE;
        when(verificationAttemptStore.increment(eq(RedisCacheKey.LOGIN_SMS_CODE_ATTEMPTS + PHONE), any(Duration.class)))
                .thenReturn(1L);
        when(verificationCodeStore.compareAndDelete(key, VerificationCodeDigest.digest("123456", HMAC_KEY)))
                .thenReturn(false);

        assertThrows(KaptchaNotMatchException.class, () -> provider.kaptchaValidate(PHONE, "123456"));

        verify(verificationCodeStore).compareAndDelete(key, VerificationCodeDigest.digest("123456", HMAC_KEY));
        verify(verificationCodeStore, never()).delete(any());
    }
}

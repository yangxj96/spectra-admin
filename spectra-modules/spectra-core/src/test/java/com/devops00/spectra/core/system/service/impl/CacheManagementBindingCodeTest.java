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
package com.devops00.spectra.core.system.service.impl;

import com.devops00.spectra.common.port.security.SecurityVerificationAttemptStore;
import com.devops00.spectra.common.port.security.SecurityVerificationCodeStore;
import com.devops00.spectra.core.common.constant.RedisCacheKey;
import com.devops00.spectra.core.system.javabean.from.SecurityVerificationClearFrom;
import com.devops00.spectra.core.system.javabean.from.SecurityVerificationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 运维入口与绑定验证码尝试预算使用同一安全 Redis 键。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/11
 */
class CacheManagementBindingCodeTest {
    @Test
    void clearingBindingCodeCanAlsoClearItsAttemptBudget() {
        var codes = mock(SecurityVerificationCodeStore.class);
        var attempts = mock(SecurityVerificationAttemptStore.class);
        var service = service(codes, attempts);
        var address = "user@example.test";

        var result = service.clearVerification(new SecurityVerificationClearFrom(
                SecurityVerificationType.BIND_EMAIL, address, true, "synthetic", true));

        assertEquals("SUCCEEDED", result.status());
        verify(codes).delete(RedisCacheKey.BIND_EMAIL_CODE + address);
        verify(attempts).delete(RedisCacheKey.BIND_EMAIL_CODE + address + ":attempts");
    }

    @Test
    void clearingOnlyCodeLeavesAttemptsForExplicitOperatorChoice() {
        var codes = mock(SecurityVerificationCodeStore.class);
        var attempts = mock(SecurityVerificationAttemptStore.class);
        var service = service(codes, attempts);
        var address = "13800000001";

        service.clearVerification(new SecurityVerificationClearFrom(
                SecurityVerificationType.BIND_PHONE, address, false, "synthetic", true));

        verify(codes).delete(RedisCacheKey.BIND_PHONE_CODE + address);
        verifyNoInteractions(attempts);
    }

    private CacheManagementServiceImpl service(SecurityVerificationCodeStore codes,
                                               SecurityVerificationAttemptStore attempts) {
        return new CacheManagementServiceImpl(null, null, null, null, null, codes, attempts, null, null);
    }
}

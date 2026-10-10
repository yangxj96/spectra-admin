/*
 * Copyright 2018-2026 yangxj96
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.devops00.spectra.core.system.service.impl;

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.core.security.authentication.identity.AuthenticationIdentifierHash;
import com.devops00.spectra.core.system.javabean.from.SecurityLoginFailureClearFrom;
import com.devops00.spectra.framework.security.session.lifecycle.SecurityLoginFailureTracker;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * 验证运维清理与登录使用同一身份摘要桶，且安全 Redis 失败不能返回成功。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/11
 */
class CacheManagementLoginFailureTest {

    @Test
    void paddedMixedCaseLongEmailClearsItsCanonicalLoginBucket() {
        var tracker = mock(SecurityLoginFailureTracker.class);
        String canonical = "Person@" + "x".repeat(60) + "." + "y".repeat(60) + ".example";
        String submitted = "  " + canonical + "  ";

        var result = service(tracker).clearLoginFailure(new SecurityLoginFailureClearFrom(submitted, "synthetic", true));

        assertEquals("SUCCEEDED", result.status());
        verify(tracker).clearLoginFail(AuthenticationIdentifierHash.digest(canonical));
    }

    @Test
    void blankIdentityAndMissingConfirmationNeverClearCounter() {
        var tracker = mock(SecurityLoginFailureTracker.class);
        var service = service(tracker);

        assertThrows(IllegalArgumentException.class,
                () -> service.clearLoginFailure(new SecurityLoginFailureClearFrom("   ", "synthetic", true)));
        assertThrows(IllegalArgumentException.class,
                () -> service.clearLoginFailure(new SecurityLoginFailureClearFrom("person@example.test", "synthetic", false)));
        verifyNoInteractions(tracker);
    }

    @Test
    void redisClearFailureDoesNotReportSuccess() {
        var tracker = mock(SecurityLoginFailureTracker.class);
        doThrow(new SecurityRedisUnavailableException("synthetic command failure", null))
                .when(tracker)
                .clearLoginFail(anyString());

        assertThrows(SecurityRedisUnavailableException.class,
                () -> service(tracker).clearLoginFailure(new SecurityLoginFailureClearFrom(
                        " PERSON@example.test ", "synthetic", true)));
    }

    private CacheManagementServiceImpl service(SecurityLoginFailureTracker tracker) {
        return new CacheManagementServiceImpl(null, null, null, null, null, null, null, tracker, null);
    }
}

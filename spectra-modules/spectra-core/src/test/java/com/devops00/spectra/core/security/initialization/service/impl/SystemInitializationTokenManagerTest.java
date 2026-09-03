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

package com.devops00.spectra.core.security.initialization.service.impl;

import com.devops00.spectra.common.port.security.SecurityInitializationTokenStore;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 系统初始化令牌管理测试。 */
class SystemInitializationTokenManagerTest {

    @Test
    void shouldCreateOnlyDigestWhenTokenDoesNotExist() {
        SecurityInitializationTokenStore tokenStore = mock();
        when(tokenStore.putIfAbsent(anyString())).thenReturn(true);

        new SystemInitializationTokenManager(tokenStore).ensureToken();

        var digest = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(tokenStore).putIfAbsent(digest.capture());
        assertTrue(digest.getValue().matches("[0-9a-f]{64}"));
    }

    @Test
    void shouldValidateDigestWithConstantTimeComparison() {
        SecurityInitializationTokenStore tokenStore = mock();
        String token = "bootstrap-token";
        when(tokenStore.getDigest()).thenReturn(java.util.Optional.of(SystemInitializationTokenManager.digest(token)));
        var manager = new SystemInitializationTokenManager(tokenStore);

        assertDoesNotThrow(() -> manager.assertToken(token));
        assertThrows(AccessDeniedException.class, () -> manager.assertToken("wrong-token"));
    }

    @Test
    void shouldFailClosedWhenRedisDoesNotReturnTokenDigest() {
        SecurityInitializationTokenStore tokenStore = mock();
        when(tokenStore.getDigest()).thenReturn(java.util.Optional.empty());

        assertThrows(AccessDeniedException.class,
                () -> new SystemInitializationTokenManager(tokenStore).assertToken("bootstrap-token"));
    }

    @Test
    void shouldDeleteDigestAfterInitialization() {
        SecurityInitializationTokenStore tokenStore = mock();

        new SystemInitializationTokenManager(tokenStore).clear();

        verify(tokenStore).clear();
    }
}

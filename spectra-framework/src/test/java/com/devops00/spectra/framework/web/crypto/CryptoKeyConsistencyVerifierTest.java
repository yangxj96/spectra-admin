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

package com.devops00.spectra.framework.web.crypto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link CryptoKeyConsistencyVerifier} 恢复一致性校验的行为测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class CryptoKeyConsistencyVerifierTest {

    @SuppressWarnings("unchecked")
    private static ObjectProvider<CryptoKeyManager> provider(CryptoKeyManager manager) {
        ObjectProvider<CryptoKeyManager> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(manager);
        return provider;
    }

    @Test
    void configuredButKeysUnavailableFailsVerification() {
        CryptoKeyManager manager = mock(CryptoKeyManager.class);
        when(manager.isConfiguredEnabled()).thenReturn(true);
        when(manager.isEnabled()).thenReturn(false);
        var verifier = new CryptoKeyConsistencyVerifier(provider(manager));
        assertThrows(IllegalStateException.class, verifier::verifyRecovery);
    }

    @Test
    void readyKeysPassVerification() {
        CryptoKeyManager manager = mock(CryptoKeyManager.class);
        when(manager.isConfiguredEnabled()).thenReturn(true);
        when(manager.isEnabled()).thenReturn(true);
        var verifier = new CryptoKeyConsistencyVerifier(provider(manager));
        assertDoesNotThrow(verifier::verifyRecovery);
    }

    @Test
    void encryptedDisabledPassesVerification() {
        CryptoKeyManager manager = mock(CryptoKeyManager.class);
        when(manager.isConfiguredEnabled()).thenReturn(false);
        var verifier = new CryptoKeyConsistencyVerifier(provider(manager));
        assertDoesNotThrow(verifier::verifyRecovery);
    }

    @Test
    void missingManagerPassesVerification() {
        var verifier = new CryptoKeyConsistencyVerifier(provider(null));
        assertDoesNotThrow(verifier::verifyRecovery);
    }
}

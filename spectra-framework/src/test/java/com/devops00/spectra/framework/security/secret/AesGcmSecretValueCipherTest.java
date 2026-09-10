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

package com.devops00.spectra.framework.security.secret;

import com.devops00.spectra.common.exception.EncryptException;
import com.devops00.spectra.common.port.security.SecretValueCipher;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 根密钥保护密钥值的 AES-GCM 契约测试。 */
class AesGcmSecretValueCipherTest {

    private static final String ROOT_KEY = Base64.getEncoder()
            .encodeToString(new byte[]{
                    0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                    16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31
            });

    @Test
    void shouldEncryptAndDecryptWithCodeBoundAdditionalData() {
        var cipher = new AesGcmSecretValueCipher(properties());

        SecretValueCipher.EncryptedValue encrypted = cipher.encrypt("secret.code", "敏感配置");

        assertEquals("AES-256-GCM", encrypted.algorithm());
        assertEquals("敏感配置", cipher.decrypt("secret.code", encrypted));
        assertThrows(EncryptException.class, () -> cipher.decrypt("another.code", encrypted));
    }

    @Test
    void shouldUseIndependentRandomNonce() {
        var cipher = new AesGcmSecretValueCipher(properties());

        var first = cipher.encrypt("secret.code", "same");
        var second = cipher.encrypt("secret.code", "same");

        assertEquals(12, first.nonce().length);
        org.junit.jupiter.api.Assertions.assertFalse(java.util.Arrays.equals(first.nonce(), second.nonce()));
        org.junit.jupiter.api.Assertions.assertFalse(java.util.Arrays.equals(first.ciphertext(), second.ciphertext()));
    }

    @Test
    void shouldRejectMissingOrInvalidRootKey() {
        var missing = new SecretMasterKeyProperties();
        var invalid = new SecretMasterKeyProperties("not-base64");

        assertThrows(EncryptException.class, () -> new AesGcmSecretValueCipher(missing).encrypt("secret.code", "value"));
        assertThrows(EncryptException.class, () -> new AesGcmSecretValueCipher(invalid).encrypt("secret.code", "value"));
    }

    private static SecretMasterKeyProperties properties() {
        return new SecretMasterKeyProperties(ROOT_KEY);
    }
}

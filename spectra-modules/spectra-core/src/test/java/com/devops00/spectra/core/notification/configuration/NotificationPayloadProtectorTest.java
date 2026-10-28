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

package com.devops00.spectra.core.notification.configuration;

import com.devops00.spectra.common.port.security.RuntimeSecret;
import com.devops00.spectra.core.notification.security.NotificationPayloadCipher;
import com.devops00.spectra.core.security.secret.service.SecretRuntimeService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.spec.SecretKeySpec;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证通知密文携带密钥版本，并兼容轮换前的无版本旧密文。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/28
 */
class NotificationPayloadProtectorTest {

    private static final String CODE = "notification.address-encryption-key";
    private static final String OLD_KEY = "MDEyMzQ1Njc4OWFiY2RlZg==";
    private static final String NEW_KEY = "ZmVkY2JhOTg3NjU0MzIxMA==";

    private final SecretRuntimeService secrets = mock(SecretRuntimeService.class);
    private final NotificationPayloadProtector protector = new NotificationPayloadProtector(secrets,
            new ObjectMapper());

    @Test
    void newCiphertextCarriesActiveVersionAndSurvivesRotation() {
        when(secrets.findActive(CODE)).thenReturn(Optional.of(secret(1, OLD_KEY)));
        var oldCiphertext = protector.protectAddress("13800000000");
        assertTrue(oldCiphertext.startsWith("v2:1:"));

        when(secrets.findActive(CODE)).thenReturn(Optional.of(secret(2, NEW_KEY)));
        when(secrets.findVersion(CODE, 1)).thenReturn(Optional.of(secret(1, OLD_KEY)));
        when(secrets.findVersion(CODE, 2)).thenReturn(Optional.of(secret(2, NEW_KEY)));
        var newCiphertext = protector.protectAddress("13900000000");
        assertTrue(newCiphertext.startsWith("v2:2:"));
        assertEquals("13800000000", protector.unprotectAddress(oldCiphertext));
        assertEquals("13900000000", protector.unprotectAddress(newCiphertext));
    }

    @Test
    void legacyCiphertextFallsBackToRetiredVersion() {
        var legacy = legacyCiphertext("13700000000", OLD_KEY);
        when(secrets.findActive(CODE)).thenReturn(Optional.of(secret(2, NEW_KEY)));
        when(secrets.findVersions(CODE)).thenReturn(List.of(secret(2, NEW_KEY), secret(1, OLD_KEY)));

        assertEquals("13700000000", protector.unprotectAddress(legacy));
    }

    private static RuntimeSecret secret(int version, String value) {
        return new RuntimeSecret(CODE, version, value, "fingerprint-" + version);
    }

    private static String legacyCiphertext(String plainText, String encodedKey) {
        try {
            var keyBytes = java.util.Base64.getDecoder().decode(encodedKey);
            var iv = NotificationPayloadCipher.generateIv();
            var cipherText = NotificationPayloadCipher.encrypt(plainText,
                    new SecretKeySpec(keyBytes, "AES"), iv);
            return "v1:" + NotificationPayloadCipher.getIvHex(iv) + ":" + cipherText;
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}

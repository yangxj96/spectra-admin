/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication.mfa.util;

import com.devops00.spectra.security.base.properties.SecurityProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TotpSecretCipherTest {

    @Test
    void shouldEncryptAndDecryptTotpSecretWithConfiguredKey() {
        SecurityProperties properties = new SecurityProperties();
        properties.setMfaEncryptionKey("01234567890123456789012345678901");
        properties.setMfaEncryptionKeyVersion("v7");
        TotpSecretCipher cipher = new TotpSecretCipher(properties);

        var encrypted = cipher.encrypt("JBSWY3DPEHPK3PXP");

        assertEquals("JBSWY3DPEHPK3PXP", cipher.decrypt(encrypted.keyVersion(), encrypted.combined()));
        assertThrows(IllegalStateException.class, () -> cipher.decrypt("v6", encrypted.combined()));
    }

    @Test
    void shouldDecryptPreviousKeyAndReencryptWithActiveKey() {
        SecurityProperties oldProperties = new SecurityProperties();
        oldProperties.setMfaEncryptionKey("01234567890123456789012345678901");
        oldProperties.setMfaEncryptionKeyVersion("v6");
        var oldCipher = new TotpSecretCipher(oldProperties);
        var oldEncrypted = oldCipher.encrypt("JBSWY3DPEHPK3PXP");

        SecurityProperties rotatedProperties = new SecurityProperties();
        rotatedProperties.setMfaEncryptionKey("12345678901234567890123456789012");
        rotatedProperties.setMfaEncryptionKeyVersion("v7");
        rotatedProperties.setMfaPreviousEncryptionKey("01234567890123456789012345678901");
        rotatedProperties.setMfaPreviousEncryptionKeyVersion("v6");
        var rotatedCipher = new TotpSecretCipher(rotatedProperties);

        assertEquals("JBSWY3DPEHPK3PXP", rotatedCipher.decrypt("v6", oldEncrypted.combined()));
        var reencrypted = rotatedCipher.reencrypt("v6", oldEncrypted.combined());
        assertEquals("v7", reencrypted.keyVersion());
        assertEquals("JBSWY3DPEHPK3PXP", rotatedCipher.decrypt("v7", reencrypted.combined()));
    }

    @Test
    void shouldRejectIncompletePreviousKeyConfiguration() {
        SecurityProperties properties = new SecurityProperties();
        properties.setMfaEncryptionKey("01234567890123456789012345678901");
        properties.setMfaPreviousEncryptionKeyVersion("v6");

        assertThrows(IllegalStateException.class, () -> new TotpSecretCipher(properties));
    }
}

/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication;

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
}

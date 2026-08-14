/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication;

import com.devops00.spectra.security.base.properties.SecurityProperties;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/** TOTP 共享密钥的 AES-GCM 加解密适配器。 */
public final class TotpSecretCipher {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final byte[] key;
    private final String keyVersion;

    public TotpSecretCipher(SecurityProperties properties) {
        String configuredKey = properties.getMfaEncryptionKey();
        if (configuredKey == null || configuredKey.getBytes(StandardCharsets.UTF_8).length != 32) {
            throw new IllegalStateException("MFA 加密密钥必须通过部署密钥管理配置为 32 字节");
        }
        this.key = configuredKey.getBytes(StandardCharsets.UTF_8);
        this.keyVersion = properties.getMfaEncryptionKeyVersion();
    }

    public EncryptedSecret encrypt(String secret) {
        try {
            byte[] iv = new byte[IV_BYTES];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(secret.getBytes(StandardCharsets.UTF_8));
            return new EncryptedSecret(keyVersion, iv, ciphertext);
        } catch (Exception exception) {
            throw new IllegalStateException("MFA 密钥加密失败", exception);
        }
    }

    public String decrypt(String keyVersion, byte[] encrypted) {
        if (!this.keyVersion.equals(keyVersion) || encrypted == null || encrypted.length <= IV_BYTES) {
            throw new IllegalStateException("MFA 密钥版本不受支持或密文无效");
        }
        try {
            byte[] iv = java.util.Arrays.copyOf(encrypted, IV_BYTES);
            byte[] ciphertext = java.util.Arrays.copyOfRange(encrypted, IV_BYTES, encrypted.length);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("MFA 密钥解密失败", exception);
        }
    }

    public record EncryptedSecret(String keyVersion, byte[] iv, byte[] ciphertext) {
        public byte[] combined() {
            byte[] result = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);
            return result;
        }

        public String encoded() {
            return Base64.getEncoder().encodeToString(combined());
        }
    }
}

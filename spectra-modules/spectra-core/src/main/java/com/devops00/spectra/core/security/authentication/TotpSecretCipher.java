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
import java.util.HashMap;
import java.util.Map;

/** TOTP 共享密钥的 AES-GCM 加解密适配器。 */
public final class TotpSecretCipher {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<String, byte[]> keys;
    private final String keyVersion;

    public TotpSecretCipher(SecurityProperties properties) {
        String configuredKey = properties.getMfaEncryptionKey();
        this.keyVersion = requireVersion(properties.getMfaEncryptionKeyVersion(), "当前");
        this.keys = new HashMap<>();
        this.keys.put(keyVersion, keyBytes(configuredKey, "当前"));

        String previousKey = properties.getMfaPreviousEncryptionKey();
        String previousVersion = properties.getMfaPreviousEncryptionKeyVersion();
        if (isBlank(previousKey) != isBlank(previousVersion)) {
            throw new IllegalStateException("MFA 上一版本密钥和版本号必须同时配置");
        }
        if (!isBlank(previousKey)) {
            previousVersion = requireVersion(previousVersion, "上一版本");
            if (keys.containsKey(previousVersion)) {
                throw new IllegalStateException("MFA 当前和上一版本号不能相同");
            }
            keys.put(previousVersion, keyBytes(previousKey, "上一版本"));
        }
    }

    public EncryptedSecret encrypt(String secret) {
        try {
            byte[] iv = new byte[IV_BYTES];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keys.get(keyVersion), "AES"), new GCMParameterSpec(TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(secret.getBytes(StandardCharsets.UTF_8));
            return new EncryptedSecret(keyVersion, iv, ciphertext);
        } catch (Exception exception) {
            throw new IllegalStateException("MFA 密钥加密失败", exception);
        }
    }

    public String decrypt(String keyVersion, byte[] encrypted) {
        byte[] key = keys.get(keyVersion);
        if (key == null || encrypted == null || encrypted.length <= IV_BYTES) {
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

    /** 判断密文是否仍使用当前活动密钥版本。 */
    public boolean isCurrentVersion(String keyVersion) {
        return this.keyVersion.equals(keyVersion);
    }

    /** 使用旧版本解密后立即用当前版本重新加密，明文不离开本适配器。 */
    public EncryptedSecret reencrypt(String keyVersion, byte[] encrypted) {
        return encrypt(decrypt(keyVersion, encrypted));
    }

    private static byte[] keyBytes(String value, String label) {
        if (value == null || value.getBytes(StandardCharsets.UTF_8).length != 32) {
            throw new IllegalStateException("MFA " + label + "加密密钥必须通过部署密钥管理配置为 32 字节");
        }
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private static String requireVersion(String value, String label) {
        if (isBlank(value) || value.length() > 64 || value.chars().anyMatch(character -> character < 0x21 || character > 0x7e)) {
            throw new IllegalStateException("MFA " + label + "密钥版本必须为 1-64 位可打印非空字符串");
        }
        return value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
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

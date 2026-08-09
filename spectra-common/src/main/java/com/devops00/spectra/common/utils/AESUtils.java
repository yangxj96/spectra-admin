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

package com.devops00.spectra.common.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES对称加密
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/6/4 16:06
 */
public class AESUtils {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 12; // 12字节推荐
    private static final int TAG_LENGTH = 128; // GCM标签长度

    /**
     * 生成随机AES密钥
     */
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE, new SecureRandom());
        return keyGenerator.generateKey();
    }

    /**
     * 生成随机IV
     */
    public static byte[] generateIv() {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * AES-GCM加密，返回Base64编码的密文
     */
    public static String encrypt(String plainText, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * AES-GCM解密
     */
    public static String decrypt(String encryptedText, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        byte[] decoded = Base64.getDecoder().decode(encryptedText);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * AES-GCM解密（接受byte[]类型的密钥）
     */
    public static String decrypt(String encryptedText, byte[] key, byte[] iv) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        return decrypt(encryptedText, secretKey, iv);
    }

    /**
     * IV转Hex字符串
     */
    public static String getIvHex(byte[] iv) {
        StringBuilder sb = new StringBuilder();
        for (byte b : iv) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Hex字符串转IV
     */
    public static byte[] hexToIv(String hex) {
        int len = hex.length();
        byte[] iv = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            iv[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return iv;
    }
}

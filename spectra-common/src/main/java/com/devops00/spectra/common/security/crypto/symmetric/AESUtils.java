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

package com.devops00.spectra.common.security.crypto.symmetric;

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
public final class AESUtils {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 12; // 12字节推荐
    private static final int TAG_LENGTH = 128; // GCM标签长度
    /** 为密钥和初始化向量提供密码学安全随机数。 */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private AESUtils() {
    }

    /**
     * 生成 256 位 AES 密钥，供 AES-GCM 加解密使用。
     *
     * @return 新生成的 AES 密钥；每次调用都生成新密钥
     * @throws Exception JCE 不支持 AES 或密钥生成失败时抛出
     */
    public static SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE, new SecureRandom());
        return keyGenerator.generateKey();
    }

    /**
     * 生成 AES-GCM 推荐长度的随机初始化向量。
     *
     * @return 12 字节随机 IV；每次调用都生成新数组
     */
    public static byte[] generateIv() {
        byte[] iv = new byte[IV_SIZE];
        SECURE_RANDOM.nextBytes(iv);
        return iv;
    }

    /**
     * 使用 AES-GCM 加密 UTF-8 明文，并返回 Base64 密文。
     *
     * @param plainText 待加密的明文，不会被记录
     * @param key       AES 密钥
     * @param iv        本次加密使用的初始化向量；同一密钥下不得复用
     * @return Base64 编码的密文，不包含 IV；调用方必须单独保存 IV
     * @throws Exception 密钥、IV、明文非法或 JCE 加密失败时抛出
     */
    public static String encrypt(String plainText, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 使用 AES-GCM 解密 Base64 密文，并按 UTF-8 还原明文。
     *
     * @param encryptedText Base64 编码的 AES-GCM 密文
     * @param key           AES 密钥
     * @param iv            加密时使用的初始化向量
     * @return 解密后的 UTF-8 明文；认证失败时不会返回部分结果
     * @throws Exception 密文、密钥或 IV 非法，或 GCM 认证失败时抛出
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
     * 使用原始字节 AES 密钥解密 Base64 编码的 AES-GCM 密文。
     *
     * @param encryptedText Base64 编码的 AES-GCM 密文
     * @param key           AES 原始密钥字节，长度必须是 JCE 支持的 AES 密钥长度
     * @param iv            加密时使用的初始化向量
     * @return 解密后的 UTF-8 明文；认证失败时不会返回部分结果
     * @throws Exception 密文、密钥或 IV 非法，或 GCM 认证失败时抛出
     */
    public static String decrypt(String encryptedText, byte[] key, byte[] iv) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        return decrypt(encryptedText, secretKey, iv);
    }

    /**
     * 将 IV 编码为小写十六进制字符串，便于协议或持久化保存。
     *
     * @param iv 待编码的初始化向量
     * @return 与输入字节一一对应的小写十六进制字符串
     */
    public static String getIvHex(byte[] iv) {
        StringBuilder sb = new StringBuilder();
        for (byte b : iv) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 将十六进制字符串还原为 IV 字节数组。
     *
     * @param hex 偶数长度的十六进制 IV 字符串
     * @return 还原后的 IV 字节数组
     * @throws IllegalArgumentException hex 含非法字符或长度为奇数时抛出
     */
    public static byte[] hexToIv(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            throw new IllegalArgumentException("IV 十六进制字符串长度必须为偶数");
        }
        int len = hex.length();
        byte[] iv = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            iv[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return iv;
    }
}

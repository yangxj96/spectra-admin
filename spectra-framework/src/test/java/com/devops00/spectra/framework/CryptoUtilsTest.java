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

package com.devops00.spectra.framework;

import com.devops00.spectra.common.utils.AESUtils;
import com.devops00.spectra.common.utils.RSAUtils;
import com.devops00.spectra.common.utils.SHA256Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import javax.crypto.SecretKey;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 加解密工具类单元测试（无需 Spring 上下文）
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/11
 */
class CryptoUtilsTest {

    private static PublicKey publicKey;
    private static PrivateKey privateKey;

    @BeforeAll
    static void setUp() throws Exception {
        KeyPair keyPair = RSAUtils.generateKeyPair();
        publicKey = keyPair.getPublic();
        privateKey = keyPair.getPrivate();
    }

    /**
     * AES-GCM 正常加解密
     */
    @Test
    void testAesEncryptDecrypt() throws Exception {
        SecretKey aesKey = AESUtils.generateKey();
        byte[] iv = AESUtils.generateIv();

        String plaintext = "Hello Spectra AES-GCM Test";
        String encrypted = AESUtils.encrypt(plaintext, aesKey, iv);
        assertNotNull(encrypted);
        assertNotEquals(plaintext, encrypted);

        String decrypted = AESUtils.decrypt(encrypted, aesKey, iv);
        assertEquals(plaintext, decrypted);
    }

    /**
     * AES-GCM byte[] 密钥解密
     */
    @Test
    void testAesEncryptDecryptWithBytes() throws Exception {
        SecretKey aesKey = AESUtils.generateKey();
        byte[] iv = AESUtils.generateIv();

        String plaintext = "Test with byte array key";
        String encrypted = AESUtils.encrypt(plaintext, aesKey, iv);
        String decrypted = AESUtils.decrypt(encrypted, aesKey.getEncoded(), iv);
        assertEquals(plaintext, decrypted);
    }

    /**
     * RSA-OAEP 加解密 AES 密钥
     */
    @Test
    void testRsaEncryptDecrypt() throws Exception {
        SecretKey aesKey = AESUtils.generateKey();
        byte[] aesKeyBytes = aesKey.getEncoded();

        String encryptedKey = RSAUtils.encrypt(aesKeyBytes, publicKey);
        assertNotNull(encryptedKey);

        byte[] decryptedKey = RSAUtils.decrypt(encryptedKey, privateKey);
        assertArrayEquals(aesKeyBytes, decryptedKey);
    }

    /**
     * RSA 签名 + 验签
     */
    @Test
    void testRsaSignVerify() throws Exception {
        String content = "data=hello&nonce=abc123&timestamp=1234567890";
        String signature = RSAUtils.sign(content, privateKey);

        assertNotNull(signature);
        assertTrue(RSAUtils.verify(content, signature, publicKey));
    }

    /**
     * RSA 验签失败（篡改数据）
     */
    @Test
    void testRsaVerifyTamperedData() throws Exception {
        String content = "data=hello&nonce=abc123&timestamp=1234567890";
        String signature = RSAUtils.sign(content, privateKey);

        assertFalse(RSAUtils.verify("data=tampered&nonce=abc123&timestamp=1234567890", signature, publicKey));
    }

    /**
     * SHA256 摘要
     */
    @Test
    void testSha256Hash() throws Exception {
        String hash1 = SHA256Utils.hash("Hello");
        String hash2 = SHA256Utils.hash("Hello");
        assertEquals(hash1, hash2);

        String hash3 = SHA256Utils.hash("World");
        assertNotEquals(hash1, hash3);
        assertEquals(64, hash1.length()); // SHA-256 输出 64 个 hex 字符
    }

    /**
     * HMAC-SHA256
     */
    @Test
    void testHmacSha256() throws Exception {
        String hmac1 = SHA256Utils.hmac("Hello", "key1");
        String hmac2 = SHA256Utils.hmac("Hello", "key1");
        assertEquals(hmac1, hmac2);

        String hmac3 = SHA256Utils.hmac("Hello", "key2");
        assertNotEquals(hmac1, hmac3);
    }

    /**
     * Nonce 生成
     */
    @Test
    void testGenerateNonce() {
        String nonce1 = SHA256Utils.generateNonce();
        String nonce2 = SHA256Utils.generateNonce();

        assertNotNull(nonce1);
        assertNotEquals(nonce1, nonce2);
    }

    /**
     * 完整的混合加密流程：AES 加密数据 → RSA 加密 AES 密钥 → RSA 解密 AES 密钥 → AES 解密数据
     */
    @Test
    void testFullMixedEncryptionRoundTrip() throws Exception {
        // 1. 生成 AES 密钥和 IV
        SecretKey aesKey = AESUtils.generateKey();
        byte[] iv = AESUtils.generateIv();

        // 2. AES-GCM 加密业务数据
        String originalData = "{\"name\":\"测试\",\"value\":12345}";
        String encryptedData = AESUtils.encrypt(originalData, aesKey, iv);

        // 3. RSA-OAEP 公钥加密 AES 密钥
        String encryptedAesKey = RSAUtils.encrypt(aesKey.getEncoded(), publicKey);

        // 4. RSA 私钥签名
        String nonce = SHA256Utils.generateNonce();
        long timestamp = System.currentTimeMillis() / 1000;
        String signContent = String.format("data=%s&nonce=%s&timestamp=%d", encryptedData, nonce, timestamp);
        String signature = RSAUtils.sign(signContent, privateKey);

        // 5. 验证签名
        assertTrue(RSAUtils.verify(signContent, signature, publicKey));

        // 6. RSA 私钥解密 AES 密钥
        byte[] decryptedAesKey = RSAUtils.decrypt(encryptedAesKey, privateKey);

        // 7. AES-GCM 解密业务数据
        String decryptedData = AESUtils.decrypt(encryptedData, decryptedAesKey, iv);

        assertEquals(originalData, decryptedData);
    }

    /**
     * IV 和 Hex 互转
     */
    @Test
    void testIvHexConversion() {
        byte[] iv = AESUtils.generateIv();
        assertEquals(12, iv.length);

        String hex = AESUtils.getIvHex(iv);
        assertEquals(24, hex.length());

        byte[] restored = AESUtils.hexToIv(hex);
        assertArrayEquals(iv, restored);
    }
}

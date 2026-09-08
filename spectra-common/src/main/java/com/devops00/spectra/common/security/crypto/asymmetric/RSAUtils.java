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

package com.devops00.spectra.common.security.crypto.asymmetric;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA 非对称加密、签名和密钥编码工具。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/6/4 16:07
 */
public final class RSAUtils {

    private static final String ALGORITHM = "RSA";
    private static final String TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int KEY_SIZE = 2048;

    private RSAUtils() {
    }

    /**
     * 生成 2048 位 RSA 密钥对。
     *
     * @return 新生成的公钥和私钥；每次调用都生成新密钥对
     * @throws Exception JCE 不支持 RSA 或密钥生成失败时抛出
     */
    public static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGenerator.initialize(KEY_SIZE, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * 从 X.509 格式的 Base64 公钥字符串恢复公钥对象。
     *
     * @param base64PublicKey Base64 编码的 X.509 公钥，不会被记录
     * @return 可用于 RSA 加密或验签的公钥
     * @throws Exception 编码、密钥格式非法或 JCE 恢复失败时抛出
     */
    public static PublicKey restorePublicKey(String base64PublicKey) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * 从 PKCS#8 格式的 Base64 私钥字符串恢复私钥对象。
     *
     * @param base64PrivateKey Base64 编码的 PKCS#8 私钥，不会被记录
     * @return 可用于 RSA 解密或签名的私钥
     * @throws Exception 编码、密钥格式非法或 JCE 恢复失败时抛出
     */
    public static PrivateKey restorePrivateKey(String base64PrivateKey) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * 使用 RSA-OAEP 公钥加密少量二进制数据，典型用途是加密 AES 密钥。
     *
     * @param data      待加密的数据，不会被记录
     * @param publicKey 接收方 RSA 公钥
     * @return Base64 编码的 RSA-OAEP 密文
     * @throws Exception 数据、密钥非法或 JCE 加密失败时抛出
     */
    public static String encrypt(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        OAEPParameterSpec oaepSpec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepSpec);
        byte[] encrypted = cipher.doFinal(data);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 使用 RSA-OAEP 私钥解密 Base64 密文。
     *
     * @param encryptedData Base64 编码的 RSA-OAEP 密文
     * @param privateKey    接收方 RSA 私钥
     * @return 解密后的原始二进制数据；解密失败时不会返回部分结果
     * @throws Exception 密文、密钥非法或 JCE 解密失败时抛出
     */
    public static byte[] decrypt(String encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        OAEPParameterSpec oaepSpec = new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepSpec);
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        return cipher.doFinal(decoded);
    }

    /**
     * 使用 SHA-256 with RSA 对 UTF-8 内容生成私钥签名。
     *
     * @param content    待签名内容，不会被记录
     * @param privateKey 签名方 RSA 私钥
     * @return Base64 编码的签名值
     * @throws Exception 内容、密钥非法或 JCE 签名失败时抛出
     */
    public static String sign(String content, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        byte[] signBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signBytes);
    }

    /**
     * 使用公钥验证 UTF-8 内容对应的 SHA-256 with RSA 签名。
     *
     * @param content      待验签内容，不会被记录
     * @param signatureStr Base64 编码的待验证签名
     * @param publicKey    签名方 RSA 公钥
     * @return 签名有效且对应内容未被修改时返回 true，否则返回 false
     * @throws Exception 签名编码、密钥非法或 JCE 验签失败时抛出
     */
    public static boolean verify(String content, String signatureStr, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(content.getBytes(StandardCharsets.UTF_8));
        byte[] signBytes = Base64.getDecoder().decode(signatureStr);
        return signature.verify(signBytes);
    }

    /**
     * 将公钥编码为可存储和传输的 Base64 字符串。
     *
     * @param publicKey 待编码的 RSA 公钥
     * @return X.509 编码公钥的 Base64 字符串
     */
    public static String getPublicKeyBase64(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * 将私钥编码为可存储和传输的 Base64 字符串。
     *
     * @param privateKey 待编码的 RSA 私钥
     * @return PKCS#8 编码私钥的 Base64 字符串；调用方不得将其写入日志
     */
    public static String getPrivateKeyBase64(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }
}

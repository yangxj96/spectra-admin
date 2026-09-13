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

package com.devops00.spectra.common.security.crypto.digest;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * SHA256摘要工具
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/6/4 16:07
 */
public final class SHA256Utils {

    private static final String SHA_256 = "SHA-256";

    private static final String HMAC_SHA_256 = "HmacSHA256";

    /** 为摘要协议所需的随机数和随机 nonce 提供密码学安全随机源。 */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private SHA256Utils() {
    }

    /**
     * 计算 UTF-8 字节序列的 SHA-256 摘要。
     *
     * @param input 待摘要的明文，不会被记录或修改
     * @return 64 位小写十六进制摘要字符串
     * @throws NullPointerException input 为 null 时抛出
     */
    public static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            return HexFormat.of().formatHex(digest.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("JDK 不支持 SHA-256 摘要算法", exception);
        }
    }

    /**
     * 使用 UTF-8 密钥计算输入的 HMAC-SHA256 摘要。
     *
     * @param input 待认证的内容，不会被记录或修改
     * @param key   HMAC 密钥，不会被记录或修改
     * @return Base64 编码的 HMAC-SHA256 摘要
     * @throws NullPointerException input 或 key 为 null 时抛出
     */
    public static String hmac(String input, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA_256));
            return Base64.getEncoder().encodeToString(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("JDK 不支持 HMAC-SHA256 摘要算法", exception);
        }
    }

    /**
     * 生成 16 字节随机 nonce，供防重放或一次性协议字段使用。
     *
     * @return Base64 编码的随机 nonce，每次调用都生成新值
     */
    public static String generateNonce() {
        byte[] nonce = new byte[16];
        SECURE_RANDOM.nextBytes(nonce);
        return Base64.getEncoder().encodeToString(nonce);
    }
}

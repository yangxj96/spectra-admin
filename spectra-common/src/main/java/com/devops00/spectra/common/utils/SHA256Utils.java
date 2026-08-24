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

    private SHA256Utils() {
    }

    /**
     * SHA-256摘要
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
     * HMAC-SHA256
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
     * 生成随机nonce（16字节）
     */
    public static String generateNonce() {
        byte[] nonce = new byte[16];
        new SecureRandom().nextBytes(nonce);
        return Base64.getEncoder().encodeToString(nonce);
    }
}

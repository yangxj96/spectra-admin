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

package com.devops00.spectra.security.base.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;

/**
 * 验证码 HMAC 摘要工具。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public final class VerificationCodeDigest {

    private static final String ALGORITHM = "HmacSHA256";

    private VerificationCodeDigest() {
    }

    /**
     * 计算验证码 HMAC 摘要。
     *
     * @param code 验证码
     * @param key  HMAC 密钥
     * @return 十六进制摘要
     */
    public static String digest(String code, String key) {
        if (code == null || key == null || key.isBlank()) {
            throw new IllegalArgumentException("验证码 HMAC 密钥未配置");
        }
        try {
            var mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(code.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("验证码摘要生成失败", exception);
        }
    }

    /**
     * 常量时间比较验证码摘要。
     *
     * @param code     待校验验证码
     * @param expected Redis 中保存的摘要
     * @param key      HMAC 密钥
     * @return 是否匹配
     */
    public static boolean matches(String code, String expected, String key) {
        if (expected == null) {
            return false;
        }
        return java.security.MessageDigest.isEqual(
                digest(code, key).getBytes(StandardCharsets.US_ASCII),
                expected.getBytes(StandardCharsets.US_ASCII));
    }
}

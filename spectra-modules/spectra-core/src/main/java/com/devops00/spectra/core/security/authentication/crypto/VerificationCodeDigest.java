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

package com.devops00.spectra.core.security.authentication.crypto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * 验证码 HMAC 摘要工具。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
public final class VerificationCodeDigest {

    private static final String ALGORITHM = "HmacSHA256";

    private VerificationCodeDigest() {
    }

    /**
     * 使用认证配置中的密钥计算验证码摘要，供验证码存储和比对使用。
     *
     * @param code 待保护的验证码明文，不会被记录
     * @param key  验证码 HMAC 密钥，不允许为空白
     * @return 小写十六进制 HMAC-SHA256 摘要，不返回明文验证码
     * @throws IllegalArgumentException 验证码或密钥为空，或密钥为空白时抛出
     */
    public static String digest(String code, String key) {
        if (code == null || key == null || key.isBlank()) {
            throw new IllegalArgumentException("验证码 HMAC 密钥未配置");
        }
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal(code.getBytes(StandardCharsets.UTF_8)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("验证码摘要生成失败", exception);
        }
    }

    /**
     * 使用常量时间比较校验验证码是否对应预期摘要。
     *
     * @param code     待校验的验证码明文，不会被记录
     * @param expected 已保存的验证码摘要；为 null 时校验失败
     * @param key      验证码 HMAC 密钥，不允许为空白
     * @return 摘要相等时返回 true；预期摘要为 null 或不匹配时返回 false
     * @throws IllegalArgumentException 验证码或密钥为空，或密钥为空白时抛出
     */
    public static boolean matches(String code, String expected, String key) {
        if (expected == null) {
            return false;
        }
        return MessageDigest.isEqual(
                digest(code, key).getBytes(StandardCharsets.US_ASCII),
                expected.getBytes(StandardCharsets.US_ASCII));
    }
}

/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.base.security.mfa;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;

/** RFC 6238 TOTP 实现，默认 30 秒窗口、6 位数字和 SHA-1。 */
public final class TotpCodeService {

    private static final String ALGORITHM = "HmacSHA1";
    private static final int PERIOD_SECONDS = 30;
    private static final int DIGITS = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private TotpCodeService() {
    }

    /** 生成 160 bit、Base32 无填充的共享密钥。 */
    public static String generateSecret() {
        byte[] secret = new byte[20];
        RANDOM.nextBytes(secret);
        return base32Encode(secret);
    }

    /**
     * 计算当前时间的一次性密码。
     *
     * <p>密钥遵循 Google Authenticator 等客户端通用的 Base32 表示。</p>
     */
    public static String code(String encodedSecret, Instant instant) {
        return code(encodedSecret, instant.getEpochSecond() / PERIOD_SECONDS);
    }

    public static String code(String encodedSecret, Clock clock) {
        return code(encodedSecret, Instant.now(clock));
    }

    /** 允许前后各一个时间片，避免客户端时钟轻微漂移。 */
    public static boolean matches(String encodedSecret, String candidate, Instant instant, int window) {
        if (candidate == null || !candidate.matches("\\d{" + DIGITS + "}") || window < 0) {
            return false;
        }
        long counter = instant.getEpochSecond() / PERIOD_SECONDS;
        for (long offset = -window; offset <= window; offset++) {
            if (java.security.MessageDigest.isEqual(code(encodedSecret, counter + offset)
                    .getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                    candidate.getBytes(java.nio.charset.StandardCharsets.US_ASCII))) {
                return true;
            }
        }
        return false;
    }

    public static boolean matches(String encodedSecret, String candidate, Clock clock, int window) {
        return matches(encodedSecret, candidate, Instant.now(clock), window);
    }

    private static String code(String encodedSecret, long counter) {
        try {
            byte[] secret = base32Decode(encodedSecret);
            byte[] digest = hmac(secret, counter);
            int offset = digest[digest.length - 1] & 0x0f;
            int binary = ((digest[offset] & 0x7f) << 24)
                    | ((digest[offset + 1] & 0xff) << 16)
                    | ((digest[offset + 2] & 0xff) << 8)
                    | (digest[offset + 3] & 0xff);
            return String.format("%0" + DIGITS + "d", binary % 1_000_000);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("TOTP密钥格式无效", exception);
        }
    }

    private static byte[] hmac(byte[] secret, long counter) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret, ALGORITHM));
            return mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(counter).array());
        } catch (Exception exception) {
            throw new IllegalStateException("TOTP算法初始化失败", exception);
        }
    }

    private static String base32Encode(byte[] bytes) {
        final char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
        StringBuilder result = new StringBuilder((bytes.length * 8 + 4) / 5);
        int buffer = 0;
        int bits = 0;
        for (byte value : bytes) {
            buffer = (buffer << 8) | (value & 0xff);
            bits += 8;
            while (bits >= 5) {
                result.append(alphabet[(buffer >>> (bits - 5)) & 31]);
                bits -= 5;
            }
        }
        if (bits > 0) {
            result.append(alphabet[(buffer << (5 - bits)) & 31]);
        }
        return result.toString();
    }

    private static byte[] base32Decode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("TOTP密钥不能为空");
        }
        String normalized = value.replace("=", "").replace(" ", "").toUpperCase(java.util.Locale.ROOT);
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        int buffer = 0;
        int bits = 0;
        for (char current : normalized.toCharArray()) {
            int digit = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".indexOf(current);
            if (digit < 0) {
                throw new IllegalArgumentException("TOTP密钥不是合法Base32");
            }
            buffer = (buffer << 5) | digit;
            bits += 5;
            if (bits >= 8) {
                output.write((buffer >>> (bits - 8)) & 0xff);
                bits -= 8;
            }
        }
        return output.toByteArray();
    }
}

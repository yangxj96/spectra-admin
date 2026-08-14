/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.security.base.security.mfa;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Recovery Code 的单向存储和校验工具。 */
public final class RecoveryCodeHasher {

    private static final String PREFIX = "v1";
    private static final int ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private RecoveryCodeHasher() {
    }

    public static String hash(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Recovery Code不能为空");
        }
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derived = derive(code, salt, ITERATIONS);
        return PREFIX + '$' + ITERATIONS + '$'
                + Base64.getUrlEncoder().withoutPadding().encodeToString(salt) + '$'
                + Base64.getUrlEncoder().withoutPadding().encodeToString(derived);
    }

    public static boolean matches(String code, String encoded) {
        if (code == null || encoded == null) {
            return false;
        }
        String[] parts = encoded.split("\\$", -1);
        if (parts.length != 4 || !PREFIX.equals(parts[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getUrlDecoder().decode(parts[2]);
            byte[] expected = Base64.getUrlDecoder().decode(parts[3]);
            return MessageDigest.isEqual(expected, derive(code, salt, iterations));
        } catch (RuntimeException exception) {
            return false;
        }
    }

    private static byte[] derive(String code, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(code.toCharArray(), salt, iterations, HASH_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception exception) {
            throw new IllegalStateException("Recovery Code 哈希算法初始化失败", exception);
        }
    }
}

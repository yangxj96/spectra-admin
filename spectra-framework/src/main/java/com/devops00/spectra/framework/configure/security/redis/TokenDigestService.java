/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.framework.configure.security.redis;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Opaque Token 的生成和摘要工具。
 *
 * <p>明文 Token 只在请求和响应边界短暂存在；Redis key、索引和 Hash 中只允许出现摘要。</p>
 */
public final class TokenDigestService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    private TokenDigestService() {
    }

    /** 生成 256 bit 的 URL-safe Opaque Token。 */
    public static String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** 计算不可逆索引摘要。 */
    public static String digest(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token不能为空");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("JDK必须提供SHA-256", exception);
        }
    }
}

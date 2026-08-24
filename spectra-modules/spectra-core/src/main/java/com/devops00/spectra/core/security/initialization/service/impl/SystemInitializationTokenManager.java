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

package com.devops00.spectra.core.security.initialization.service.impl;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.security.base.constant.SecurityRedisKey;
import com.devops00.spectra.security.base.util.SecurityRedisExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * 首次系统初始化令牌管理器。
 *
 * <p>令牌只在首次生成时写入启动日志，Redis 只保存 SHA-256 摘要。应用重启时通过
 * {@code SET NX} 复用已有摘要，避免多实例启动时互相覆盖令牌。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/20
 */
@Slf4j
@Component
public class SystemInitializationTokenManager {

    private static final int TOKEN_BYTES = 32;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RedisTemplate<String, Object> redisTemplate;

    public SystemInitializationTokenManager(
                                            @Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 在系统仍未初始化时创建引导令牌；已存在的令牌不会被覆盖。
     */
    public void ensureToken() {
        String token = generateToken();
        String digest = digest(token);
        Boolean created = SecurityRedisExecutor.require("写入系统初始化令牌",
                () -> redisTemplate.opsForValue().setIfAbsent(tokenKey(), digest));
        if (Boolean.TRUE.equals(created)) {
            log.warn(LogPrefix.SECURITY.f("系统尚未初始化，请使用以下一次性初始化令牌：{}；令牌仅在本次生成时显示，" +
                    "完成初始化后会自动清理，请勿写入共享日志或工单"), token);
        } else {
            log.info(LogPrefix.SECURITY.f("系统尚未初始化，初始化令牌已存在，请使用首次生成时的启动日志中的令牌"));
        }
    }

    /**
     * 校验初始化请求携带的令牌。
     *
     * @param token 初始化请求令牌
     */
    public void assertToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AccessDeniedException("系统初始化令牌无效");
        }
        Object stored = SecurityRedisExecutor.require("读取系统初始化令牌",
                () -> redisTemplate.opsForValue().get(tokenKey()));
        if (!(stored instanceof String expectedDigest)
                || !MessageDigest.isEqual(expectedDigest.getBytes(StandardCharsets.US_ASCII),
                        digest(token).getBytes(StandardCharsets.US_ASCII))) {
            throw new AccessDeniedException("系统初始化令牌无效");
        }
    }

    /**
     * 系统完成初始化后删除引导令牌摘要。
     */
    public void clear() {
        SecurityRedisExecutor.require("删除系统初始化令牌",
                () -> redisTemplate.delete(tokenKey()));
    }

    /**
     * 转换、解析或规范化数据（{@code digest}）。
     */
    static String digest(String token) {
        try {
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance("SHA-256")
                            .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 算法不可用", exception);
        }
    }

    /**
     * 创建或构建目标数据（{@code generateToken}）。
     */
    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 转换、解析或规范化数据（{@code tokenKey}）。
     */
    private String tokenKey() {
        return SecurityRedisKey.INITIALIZATION_TOKEN.getPattern();
    }
}

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

package com.devops00.spectra.security.starter.web.service.impl;

import com.devops00.spectra.common.constant.RedisCacheKey;
import com.devops00.spectra.common.exception.SpectraException;
import com.devops00.spectra.common.notification.*;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.base.util.VerificationCodeDigest;
import com.devops00.spectra.security.starter.web.service.AuthService;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 认证验证码服务；负责生成、摘要存储和通过通知 Gateway 入队。
 */
@Service
@NullMarked
public class AuthServiceImpl implements AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final NotificationGateway notificationGateway;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SecurityProperties securityProperties;

    public AuthServiceImpl(NotificationGateway notificationGateway,
                           @Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redisTemplate,
                           SecurityProperties securityProperties) {
        this.notificationGateway = notificationGateway;
        this.redisTemplate = redisTemplate;
        this.securityProperties = securityProperties;
    }

    @Override
    public void sendSmsCode(String phone) {
        sendCode(phone, NotificationChannel.SMS, RedisCacheKey.SMS_CODE, "security.login-code.sms");
    }

    @Override
    public void sendEmailCode(String email) {
        sendCode(email, NotificationChannel.EMAIL, RedisCacheKey.EMAIL_CODE, "security.login-code.email");
    }

    private void sendCode(String address, NotificationChannel channel, String redisPrefix, String templateCode) {
        var availability = notificationGateway.availability(channel);
        if (!availability.available()) {
            throw new SpectraException("验证码通知渠道暂不可用: " + availability.reason());
        }
        var redisKey = redisPrefix + address;
        requireHmacKey();
        var code = generateCode();
        try {
            var requestWindow = Instant.now().getEpochSecond() / Math.max(1L, securityProperties.getVerificationCodeExpire());
            var stored = redisTemplate.opsForValue()
                    .setIfAbsent(redisKey, digest(code),
                            securityProperties.getVerificationCodeExpire(), TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(stored)) {
                return;
            }
            var request = new NotificationRequest(null, "security:login-code:" + channel.name() + ":" + address + ":" + requestWindow,
                    NotificationPurpose.LOGIN_CODE, List.of(channel), List.of(),
                    List.of(new NotificationDirectAddress(channel, address)), templateCode,
                    Map.of("title", "登录验证码", "content", "您的验证码为 {{code}}，请在有效期内完成操作。"),
                    Map.of("code", code), "SECURITY", channel.name() + ":" + address + ":" + requestWindow, "SECURITY", null,
                    Instant.now(), Instant.now().plusSeconds(securityProperties.getVerificationCodeExpire()), 100, null);
            NotificationReceipt receipt = notificationGateway.enqueue(request);
            if (receipt.taskCount() == 0) {
                throw new SpectraException("验证码通知未生成投递任务");
            }
        } catch (RuntimeException exception) {
            redisTemplate.delete(redisKey);
            if (exception instanceof SpectraException) {
                throw exception;
            }
            throw new SpectraException("验证码通知入队失败", exception);
        }
    }

    private String generateCode() {
        if (securityProperties.getVerificationCodeLength() != 6) {
            throw new SpectraException("验证码长度配置必须为 6 位");
        }
        return "%06d".formatted(RANDOM.nextInt(1_000_000));
    }

    private String digest(String code) {
        return VerificationCodeDigest.digest(code, securityProperties.getVerificationCodeHmacKey());
    }

    private void requireHmacKey() {
        if (securityProperties.getVerificationCodeHmacKey() == null
                || securityProperties.getVerificationCodeHmacKey().isBlank()) {
            throw new SpectraException("验证码安全密钥未配置");
        }
    }
}

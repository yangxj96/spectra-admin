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

package com.devops00.spectra.core.security.authentication.service.impl;

import com.devops00.spectra.common.constant.RedisCacheKey;
import com.devops00.spectra.common.exception.SpectraException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationDirectAddress;
import com.devops00.spectra.common.notification.NotificationGateway;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationReceipt;
import com.devops00.spectra.common.notification.NotificationRequest;
import com.devops00.spectra.core.security.authentication.service.VerificationCodeService;
import com.devops00.spectra.security.base.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.base.util.SecurityRedisExecutor;
import com.devops00.spectra.security.base.util.VerificationCodeDigest;
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
 * 认证验证码生成、摘要存储和通知投递服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final NotificationGateway notificationGateway;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SecurityProperties securityProperties;

    public VerificationCodeServiceImpl(NotificationGateway notificationGateway,
                                       @Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redisTemplate,
                                       SecurityProperties securityProperties) {
        this.notificationGateway = notificationGateway;
        this.redisTemplate = redisTemplate;
        this.securityProperties = securityProperties;
    }

    @Override
    public void sendSmsCode(String phone) {
        sendCode(phone, NotificationChannel.SMS, RedisCacheKey.LOGIN_SMS_CODE, NotificationPurpose.LOGIN_CODE,
                "security.login-code.sms", "登录验证码");
    }

    @Override
    public void sendEmailCode(String email) {
        sendCode(email, NotificationChannel.EMAIL, RedisCacheKey.LOGIN_EMAIL_CODE, NotificationPurpose.LOGIN_CODE,
                "security.login-code.email", "登录验证码");
    }

    @Override
    public void sendBindingSmsCode(String phone) {
        sendCode(phone, NotificationChannel.SMS, RedisCacheKey.BIND_PHONE_CODE,
                NotificationPurpose.BIND_PHONE_CODE, "security.bind-phone-code.sms", "绑定手机号验证码");
    }

    @Override
    public void sendBindingEmailCode(String email) {
        sendCode(email, NotificationChannel.EMAIL, RedisCacheKey.BIND_EMAIL_CODE,
                NotificationPurpose.BIND_EMAIL_CODE, "security.bind-email-code.email", "绑定邮箱验证码");
    }

    private void sendCode(String address, NotificationChannel channel, String redisPrefix,
                          NotificationPurpose purpose, String templateCode, String title) {
        var availability = notificationGateway.availability(channel);
        if (!availability.available()) {
            throw new SpectraException("验证码通知渠道暂不可用: " + availability.reason());
        }
        var redisKey = redisPrefix + address;
        requireHmacKey();
        var code = generateCode();
        try {
            var requestWindow = Instant.now().getEpochSecond()
                    / Math.max(1L, securityProperties.getVerificationCodeExpire());
            var stored = SecurityRedisExecutor.require("写入验证码", () -> redisTemplate.opsForValue()
                    .setIfAbsent(redisKey, digest(code), securityProperties.getVerificationCodeExpire(), TimeUnit.SECONDS));
            if (Boolean.FALSE.equals(stored)) {
                return;
            }
            var purposeKey = purpose == NotificationPurpose.LOGIN_CODE
                    ? "login-code"
                    : purpose.name().toLowerCase().replace('_', '-');
            var request = new NotificationRequest(null,
                    "security:" + purposeKey + ":" + channel.name() + ":" + address + ":" + requestWindow,
                    purpose, List.of(channel), List.of(), List.of(new NotificationDirectAddress(channel, address)),
                    templateCode, Map.of("title", title, "content", "您的验证码为 {{code}}，请在有效期内完成操作。"),
                    Map.of("code", code), "SECURITY", channel.name() + ":" + address + ":" + requestWindow,
                    "SECURITY", null, Instant.now(),
                    Instant.now().plusSeconds(securityProperties.getVerificationCodeExpire()), 100, null);
            NotificationReceipt receipt = notificationGateway.enqueue(request);
            if (receipt.taskCount() == 0) {
                throw new SpectraException("验证码通知未生成投递任务");
            }
        } catch (SecurityRedisUnavailableException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            SecurityRedisExecutor.run("清理验证码", () -> redisTemplate.delete(redisKey));
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

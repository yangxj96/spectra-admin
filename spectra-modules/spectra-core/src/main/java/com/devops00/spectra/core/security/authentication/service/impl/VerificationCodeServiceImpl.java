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

import com.devops00.spectra.core.common.constant.RedisCacheKey;
import com.devops00.spectra.common.exception.SpectraException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationDirectAddress;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationReceipt;
import com.devops00.spectra.common.notification.NotificationSendRequest;
import com.devops00.spectra.common.notification.NotificationService;
import com.devops00.spectra.common.notification.NotificationTemplateCode;
import com.devops00.spectra.core.security.authentication.service.VerificationCodeService;
import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.framework.configure.security.properties.SecurityProperties;
import com.devops00.spectra.common.security.crypto.VerificationCodeDigest;
import com.devops00.spectra.common.port.security.SecurityVerificationCodeStore;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;

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

    private final NotificationService notificationService;
    private final SecurityVerificationCodeStore verificationCodeStore;
    private final SecurityProperties securityProperties;

    public VerificationCodeServiceImpl(NotificationService notificationService,
                                       SecurityVerificationCodeStore verificationCodeStore,
                                       SecurityProperties securityProperties) {
        this.notificationService = notificationService;
        this.verificationCodeStore = verificationCodeStore;
        this.securityProperties = securityProperties;
    }

    @Override
    public void sendSmsCode(String phone) {
        sendCode(phone, NotificationChannel.SMS, RedisCacheKey.LOGIN_SMS_CODE, NotificationPurpose.LOGIN_CODE,
                NotificationTemplateCode.SECURITY_LOGIN_CODE);
    }

    @Override
    public void sendEmailCode(String email) {
        sendCode(email, NotificationChannel.EMAIL, RedisCacheKey.LOGIN_EMAIL_CODE, NotificationPurpose.LOGIN_CODE,
                NotificationTemplateCode.SECURITY_LOGIN_CODE);
    }

    @Override
    public void sendBindingSmsCode(String phone) {
        sendCode(phone, NotificationChannel.SMS, RedisCacheKey.BIND_PHONE_CODE,
                NotificationPurpose.BIND_PHONE_CODE, NotificationTemplateCode.SECURITY_BIND_PHONE_CODE);
    }

    @Override
    public void sendBindingEmailCode(String email) {
        sendCode(email, NotificationChannel.EMAIL, RedisCacheKey.BIND_EMAIL_CODE,
                NotificationPurpose.BIND_EMAIL_CODE, NotificationTemplateCode.SECURITY_BIND_EMAIL_CODE);
    }

    /**
     * 更新或推进目标状态（{@code sendCode}）。
     */
    private void sendCode(String address, NotificationChannel channel, String redisPrefix,
                          NotificationPurpose purpose, String templateCode) {
        var redisKey = redisPrefix + address;
        requireHmacKey();
        var code = generateCode();
        try {
            var requestWindow = Instant.now().getEpochSecond()
                    / Math.max(1L, securityProperties.getVerificationCodeExpire());
            var stored = verificationCodeStore.saveIfAbsent(redisKey, digest(code),
                    java.time.Duration.ofSeconds(securityProperties.getVerificationCodeExpire()));
            if (!stored) {
                return;
            }
            var purposeKey = purpose == NotificationPurpose.LOGIN_CODE
                    ? "login-code"
                    : purpose.name().toLowerCase().replace('_', '-');
            var now = Instant.now();
            NotificationReceipt receipt = notificationService.send(NotificationSendRequest.direct(
                    "security:" + purposeKey + ":" + channel.name() + ":" + address + ":" + requestWindow,
                    purpose, List.of(new NotificationDirectAddress(channel, address)), templateCode)
                    .sensitiveParameter("code", code)
                    .businessReference("SECURITY", channel.name() + ":" + address + ":" + requestWindow)
                    .sourceModule("SECURITY")
                    .scheduledAt(now)
                    .expiresAt(now.plusSeconds(securityProperties.getVerificationCodeExpire()))
                    .priority(100)
                    .build());
            if (receipt.taskCount() == 0) {
                throw new SpectraException("验证码通知未生成投递任务");
            }
        } catch (SecurityRedisUnavailableException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            verificationCodeStore.delete(redisKey);
            if (exception instanceof SpectraException) {
                throw exception;
            }
            throw new SpectraException("验证码通知入队失败", exception);
        }
    }

    /**
     * 创建或构建目标数据（{@code generateCode}）。
     */
    private String generateCode() {
        if (securityProperties.getVerificationCodeLength() != 6) {
            throw new SpectraException("验证码长度配置必须为 6 位");
        }
        return "%06d".formatted(RANDOM.nextInt(1_000_000));
    }

    /**
     * 转换、解析或规范化数据（{@code digest}）。
     */
    private String digest(String code) {
        return VerificationCodeDigest.digest(code, securityProperties.getVerificationCodeHmacKey());
    }

    /**
     * 校验并确保数据满足当前约束（{@code requireHmacKey}）。
     */
    private void requireHmacKey() {
        if (securityProperties.getVerificationCodeHmacKey() == null
                || securityProperties.getVerificationCodeHmacKey().isBlank()) {
            throw new SpectraException("验证码安全密钥未配置");
        }
    }

}

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

package com.devops00.spectra.core.notification.sender;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.core.notification.javabean.entity.NotificationEmailConfigEntity;
import com.devops00.spectra.core.notification.javabean.enums.EmailProviderType;
import com.devops00.spectra.core.notification.javabean.enums.EmailSslMode;
import com.devops00.spectra.core.notification.mapper.NotificationEmailConfigMapper;
import com.devops00.spectra.core.notification.properties.NotificationModuleProperties;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 邮件同步发送器，支持 SMTP 和内置模拟发送。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSender implements NotificationSender {

    private final NotificationEmailConfigMapper configMapper;
    private final NotificationModuleProperties properties;

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public SendOutcome send(NotificationSendTarget target, RenderedMessage message) {
        var config = activeConfig();
        if (config == null) {
            return SendOutcome.failure("EMAIL", null, "EMAIL_CONFIG_NOT_FOUND", "未配置启用中的邮件渠道");
        }
        if (!StringUtils.hasText(target.address())) {
            return SendOutcome.failure(config.getProvider(), config.getId(), "EMAIL_ADDRESS_MISSING", "收件邮箱为空");
        }
        if (!properties.isEnabled()) {
            return SendOutcome.failure(config.getProvider(), config.getId(), "NOTIFICATION_DISABLED", "通知模块未启用");
        }
        if (properties.isForceMock() || EmailProviderType.MOCK.name().equals(config.getProvider())) {
            log.info("模拟邮件发送: configId={}, recipient={}", config.getId(), target.address());
            return SendOutcome.success(EmailProviderType.MOCK.name(), config.getId());
        }
        return sendSmtp(config, target, message);
    }

    /**
     * 查询当前启用中的邮件配置。
     *
     * @return 启用配置；不存在时返回 {@code null}
     */
    private NotificationEmailConfigEntity activeConfig() {
        return configMapper.selectOne(new LambdaQueryWrapper<NotificationEmailConfigEntity>()
                .eq(NotificationEmailConfigEntity::getActive, true)
                .isNull(NotificationEmailConfigEntity::getDeleted)
                .last("LIMIT 1"));
    }

    /**
     * 通过 SMTP 发送邮件。
     *
     * @param config  邮件配置
     * @param target  发送目标
     * @param message 渲染内容
     * @return 发送结果
     */
    private SendOutcome sendSmtp(NotificationEmailConfigEntity config, NotificationSendTarget target,
                                 RenderedMessage message) {
        try {
            var mail = new MimeMessage(session(config));
            mail.setFrom(fromAddress(config));
            mail.setRecipient(Message.RecipientType.TO, new InternetAddress(target.address()));
            mail.setSubject(message.title(), StandardCharsets.UTF_8.name());
            if (StringUtils.hasText(message.htmlContent())) {
                mail.setContent(message.htmlContent(), "text/html; charset=UTF-8");
            } else {
                mail.setText(message.content(), StandardCharsets.UTF_8.name());
            }
            Transport.send(mail, config.getUsername(), config.getPassword());
            return SendOutcome.success(config.getProvider(), config.getId());
        } catch (MessagingException | UnsupportedEncodingException exception) {
            log.warn("SMTP 邮件发送失败: configId={}", config.getId(), exception);
            return SendOutcome.failure(config.getProvider(), config.getId(), "EMAIL_SEND_FAILED", "邮件发送失败");
        }
    }

    /**
     * 创建 SMTP 会话。
     *
     * @param config 邮件配置
     * @return 邮件会话
     */
    private Session session(NotificationEmailConfigEntity config) {
        var mailProperties = new Properties();
        mailProperties.put("mail.smtp.host", config.getHost());
        mailProperties.put("mail.smtp.port", String.valueOf(config.getPort()));
        mailProperties.put("mail.smtp.auth", "true");
        mailProperties.put("mail.smtp.connectiontimeout", String.valueOf(properties.getTimeoutMs()));
        mailProperties.put("mail.smtp.timeout", String.valueOf(properties.getTimeoutMs()));
        mailProperties.put("mail.smtp.writetimeout", String.valueOf(properties.getTimeoutMs()));
        var sslMode = config.getSslMode() == null ? EmailSslMode.NONE.name() : config.getSslMode();
        mailProperties.put("mail.smtp.ssl.enable", String.valueOf(EmailSslMode.SSL.name().equals(sslMode)));
        mailProperties.put("mail.smtp.starttls.enable", String.valueOf(EmailSslMode.STARTTLS.name().equals(sslMode)));
        mailProperties.put("mail.smtp.starttls.required", String.valueOf(EmailSslMode.STARTTLS.name().equals(sslMode)));
        return Session.getInstance(mailProperties);
    }

    /**
     * 创建发件人地址。
     *
     * @param config 邮件配置
     * @return 发件人地址
     * @throws MessagingException           地址解析失败时抛出
     * @throws UnsupportedEncodingException 编码失败时抛出
     */
    private InternetAddress fromAddress(NotificationEmailConfigEntity config)
            throws MessagingException, UnsupportedEncodingException {
        return StringUtils.hasText(config.getFromName())
                ? new InternetAddress(config.getFromAddress(), config.getFromName(), StandardCharsets.UTF_8.name())
                : new InternetAddress(config.getFromAddress());
    }
}

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

package com.devops00.spectra.core.notification.provider.impl;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.core.notification.configuration.NotificationPayloadProtector;
import com.devops00.spectra.core.notification.javabean.domain.ChannelSendResult;
import com.devops00.spectra.core.notification.javabean.domain.NotificationProviderConfiguration;
import com.devops00.spectra.core.notification.javabean.domain.NotificationProviderHealth;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.core.notification.provider.NotificationProvider;
import com.devops00.spectra.core.notification.provider.NotificationTaskMessage;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

/**
 * SMTP 邮件 Provider；支持 465 隐式 SSL 和 587 STARTTLS 两种常见部署方式。
 */
@Component
@RequiredArgsConstructor
public class SmtpEmailNotificationProvider implements NotificationProvider {

    private static final String CODE = "SMTP";

    private final NotificationPayloadProtector payloadProtector;

    @Override
    public String code() {
        return CODE;
    }

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.EMAIL;
    }

    @Override
    public NotificationProviderHealth health(NotificationProviderConfiguration configuration) {
        var checkedAt = Instant.now();
        if (!usable(configuration)) {
            return NotificationProviderHealth.blocked("PROVIDER_CONFIGURATION_INVALID", checkedAt);
        }
        try {
            var session = session(configuration);
            try (var transport = session.getTransport("smtp")) {
                transport.connect(configuration.endpoint(), configuration.port(), configuration.credentialId(),
                        configuration.secret());
            }
            return NotificationProviderHealth.healthy("HEALTH_CHECK_OK", checkedAt);
        } catch (MessagingException exception) {
            return NotificationProviderHealth.unhealthy("HEALTH_CHECK_UNAVAILABLE", checkedAt);
        }
    }

    @Override
    public ChannelSendResult send(NotificationTaskEntity task, NotificationProviderConfiguration configuration) {
        if (!usable(configuration)) {
            return ChannelSendResult.blocked(CODE, null, "PROVIDER_CONFIGURATION_INVALID");
        }
        final String recipient;
        try {
            recipient = payloadProtector.unprotectAddress(task.getRecipientCiphertext());
        } catch (RuntimeException exception) {
            return ChannelSendResult.blocked(CODE, null, "RECIPIENT_ADDRESS_UNAVAILABLE");
        }
        try {
            var messageContent = NotificationTaskMessage.resolve(task, payloadProtector);
            var message = new MimeMessage(session(configuration));
            var senderName = configuration.senderName();
            var from = senderName == null || senderName.isBlank()
                    ? new InternetAddress(configuration.senderAddress())
                    : new InternetAddress(configuration.senderAddress(), senderName, StandardCharsets.UTF_8.name());
            message.setFrom(from);
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setSubject(messageContent.title(), StandardCharsets.UTF_8.name());
            message.setText(messageContent.content(), StandardCharsets.UTF_8.name());
            Transport.send(message, configuration.credentialId(), configuration.secret());
            return ChannelSendResult.sent(CODE, "smtp-" + UUID.randomUUID(), "PROVIDER_ACCEPTED");
        } catch (MessagingException | UnsupportedEncodingException exception) {
            return ChannelSendResult.failed("PROVIDER_REJECTED", null, "PROVIDER_REJECTED");
        } catch (RuntimeException exception) {
            return ChannelSendResult.unknown(CODE, null, "PROVIDER_REQUEST_UNAVAILABLE");
        }
    }

    /**
     * 处理内部业务逻辑（{@code session}）。
     */
    private Session session(NotificationProviderConfiguration configuration) {
        var properties = new Properties();
        properties.put("mail.smtp.host", configuration.endpoint());
        properties.put("mail.smtp.port", String.valueOf(configuration.port()));
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.connectiontimeout", String.valueOf(configuration.timeoutMs()));
        properties.put("mail.smtp.timeout", String.valueOf(configuration.timeoutMs()));
        properties.put("mail.smtp.writetimeout", String.valueOf(configuration.timeoutMs()));
        properties.put("mail.smtp.ssl.enable", String.valueOf(configuration.sslEnabled()));
        properties.put("mail.smtp.starttls.enable", String.valueOf(configuration.starttlsEnabled()));
        properties.put("mail.smtp.starttls.required", String.valueOf(configuration.starttlsEnabled()));
        return Session.getInstance(properties);
    }

    /**
     * 处理内部业务逻辑（{@code usable}）。
     */
    private boolean usable(NotificationProviderConfiguration configuration) {
        return configuration != null
                && configuration.enabled()
                && CODE.equals(configuration.providerType())
                && configuration.endpoint() != null
                && !configuration.endpoint().isBlank()
                && configuration.port() > 0
                && configuration.credentialId() != null
                && !configuration.credentialId().isBlank()
                && configuration.senderAddress() != null
                && !configuration.senderAddress().isBlank()
                && configuration.secret() != null
                && !configuration.secret().isBlank()
                && configuration.timeoutMs() >= 100;
    }
}

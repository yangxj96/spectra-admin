package com.devops00.spectra.core.notification.gateway;

import java.util.UUID;

import com.devops00.spectra.core.notification.constant.NotificationChannel;
import com.devops00.spectra.core.notification.service.NotificationGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 通知渠道模拟实现，仅用于本地联调和自动化测试。
 * 启用配置：{@code SPECTRA_NOTIFICATION_MOCK_ENABLED=true}。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "spectra.notification.mock", name = "enabled", havingValue = "true")
public class MockNotificationGateway implements NotificationGateway {

    @Override
    public boolean supports(NotificationChannel channel) {
        return channel == NotificationChannel.SMS || channel == NotificationChannel.EMAIL;
    }

    @Override
    public String send(NotificationChannel channel, String address, String title, String content) {
        if (!supports(channel)) {
            throw new IllegalArgumentException("Mock Gateway 不支持该渠道");
        }
        if (address == null || address.isBlank() || title == null || title.isBlank() || content == null || content.isBlank()) {
            throw new IllegalArgumentException("模拟通知参数不完整");
        }
        var messageId = "mock-" + UUID.randomUUID();
        log.info("模拟通知已发送: channel={}, address={}, messageId={}", channel, mask(address), messageId);
        return messageId;
    }

    private String mask(String address) {
        if (address.length() <= 4) {
            return "****";
        }
        return address.substring(0, 2) + "****" + address.substring(address.length() - 2);
    }
}

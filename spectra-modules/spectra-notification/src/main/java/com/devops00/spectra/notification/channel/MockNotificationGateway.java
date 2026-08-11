package com.devops00.spectra.notification.channel;

import java.util.UUID;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationGateway;
import com.devops00.spectra.common.notification.NotificationRequest;
import com.devops00.spectra.common.notification.NotificationReceipt;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** 独立通知模块的 Mock Gateway，仅用于本地联调。 */
@Component
@ConditionalOnProperty(prefix = "spectra.notification.mock", name = "enabled", havingValue = "true")
public class MockNotificationGateway implements NotificationGateway {

    @Override
    public NotificationReceipt enqueue(NotificationRequest request) {
        return new NotificationReceipt(request.requestId() == null ? UUID.randomUUID() : request.requestId(),
                "MOCK_ACCEPTED", request.recipientUserIds().size() * Math.max(1, request.channels().size()), false);
    }
}

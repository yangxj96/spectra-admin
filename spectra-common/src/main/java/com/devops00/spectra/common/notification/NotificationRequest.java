package com.devops00.spectra.common.notification;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 业务模块提交的不可变通知请求契约。
 * 实现模块不得接受任务状态、Provider Bean 名或重试字段。
 */
public record NotificationRequest(
        UUID requestId,
        String idempotencyKey,
        NotificationPurpose purpose,
        List<NotificationChannel> channels,
        List<UUID> recipientUserIds,
        String templateGroupCode,
        Map<String, Object> parameters,
        String businessType,
        String businessId,
        String sourceModule,
        UUID sourceDepartmentId,
        Instant scheduledAt,
        Instant expiresAt,
        Integer priority,
        String link) {

    public NotificationRequest {
        channels = channels == null ? List.of() : List.copyOf(channels);
        recipientUserIds = recipientUserIds == null ? List.of() : List.copyOf(recipientUserIds);
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
    }
}

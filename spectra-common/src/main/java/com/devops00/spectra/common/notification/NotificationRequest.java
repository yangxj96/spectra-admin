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
        Map<String, Object> sensitiveParameters,
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
        sensitiveParameters = sensitiveParameters == null ? Map.of() : Map.copyOf(sensitiveParameters);
    }

    /**
     * 兼容尚未迁移的调用方；敏感参数默认为空，业务方不得把验证码等敏感值放入普通参数。
     */
    public NotificationRequest(UUID requestId, String idempotencyKey, NotificationPurpose purpose,
            List<NotificationChannel> channels, List<UUID> recipientUserIds, String templateGroupCode,
            Map<String, Object> parameters, String businessType, String businessId, String sourceModule,
            UUID sourceDepartmentId, Instant scheduledAt, Instant expiresAt, Integer priority, String link) {
        this(requestId, idempotencyKey, purpose, channels, recipientUserIds, templateGroupCode, parameters, Map.of(),
                businessType, businessId, sourceModule, sourceDepartmentId, scheduledAt, expiresAt, priority, link);
    }

    /** 创建只投递站内信的业务通知请求，正文作为非敏感模板参数进入通知模块。 */
    public static NotificationRequest inApp(String idempotencyKey, NotificationPurpose purpose,
            List<UUID> recipientUserIds, String templateGroupCode, String title, String content,
            String businessType, String businessId, String sourceModule, String link) {
        var parameters = new java.util.HashMap<String, Object>();
        parameters.put("title", title == null ? "通知" : title);
        parameters.put("content", content == null ? "" : content);
        return new NotificationRequest(null, idempotencyKey, purpose, List.of(NotificationChannel.IN_APP),
                recipientUserIds, templateGroupCode, parameters, businessType, businessId, sourceModule, null, null, null,
                0, link);
    }
}

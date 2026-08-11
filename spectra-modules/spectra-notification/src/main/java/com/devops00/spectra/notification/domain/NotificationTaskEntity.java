package com.devops00.spectra.notification.domain;

import java.time.Instant;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 通知任务实体。 */
@Data
@TableName(value = "ntf_task", schema = "spectra_notification")
public class NotificationTaskEntity {
    private UUID id;
    private UUID requestId;
    private UUID tenantId;
    private UUID recipientUserId;
    private String recipientAddress;
    private String channel;
    private String title;
    private String content;
    private Instant scheduledAt;
    private String status;
    private Integer retryCount;
    private String lastError;
    private Instant createdAt;
    private Instant updatedAt;
}

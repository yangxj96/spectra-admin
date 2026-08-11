package com.devops00.spectra.notification.domain;

import java.time.Instant;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 站内信收件箱实体。 */
@Data
@TableName(value = "ntf_inbox", schema = "spectra_notification")
public class NotificationInboxEntity {
    private UUID id;
    private UUID tenantId;
    private UUID recipientUserId;
    private UUID requestId;
    private String title;
    private String content;
    private Instant readAt;
    private Instant archivedAt;
    private Instant createdAt;
}

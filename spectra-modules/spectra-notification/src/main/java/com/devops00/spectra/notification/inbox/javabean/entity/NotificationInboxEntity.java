package com.devops00.spectra.notification.inbox.javabean.entity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.mybatis.PgJsonbTypeHandler;
import lombok.Data;

/** 站内信收件箱实体。 */
@Data
@TableName(value = "ntf_inbox_message", schema = "spectra_notification", autoResultMap = true)
public class NotificationInboxEntity {
    private UUID id;
    private UUID tenantId;
    private UUID recipientUserId;
    private UUID requestId;
    private UUID taskId;
    private String purpose;
    private String title;
    private String content;
    private UUID senderUserId;
    private String senderName;
    private String link;
    @TableField(typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> extra;
    private Instant readAt;
    private Instant archivedAt;
    private Instant deleted;
    private Instant createdAt;
}

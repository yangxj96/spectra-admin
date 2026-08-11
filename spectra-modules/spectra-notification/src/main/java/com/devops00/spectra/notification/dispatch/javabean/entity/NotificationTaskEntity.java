package com.devops00.spectra.notification.dispatch.javabean.entity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.mybatis.PgJsonbTypeHandler;
import lombok.Data;

/** 通知任务实体。 */
@Data
@TableName(value = "ntf_task", schema = "spectra_notification", autoResultMap = true)
public class NotificationTaskEntity {
    private UUID id;
    private UUID requestId;
    private UUID tenantId;
    private UUID recipientUserId;
    private String recipientAddress;
    private String channel;
    private String purpose;
    private String title;
    private String content;
    private String link;
    @TableField(typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> extra;
    private Instant scheduledAt;
    private String status;
    private Integer retryCount;
    private String lastError;
    private Instant createdAt;
    private Instant updatedAt;
}

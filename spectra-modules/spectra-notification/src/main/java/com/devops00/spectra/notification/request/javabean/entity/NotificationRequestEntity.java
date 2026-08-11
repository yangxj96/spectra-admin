package com.devops00.spectra.notification.request.javabean.entity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.mybatis.PgJsonbTypeHandler;
import lombok.Data;

/** 通知请求实体。 */
@Data
@TableName(value = "ntf_request", schema = "spectra_notification", autoResultMap = true)
public class NotificationRequestEntity {
    private UUID id;
    private UUID tenantId;
    private String businessType;
    private String businessId;
    private String idempotencyKey;
    private String templateCode;
    private String purpose;
    private UUID senderUserId;
    private String sourceModule;
    private UUID sourceDepartmentId;
    private String dataScopeKey;
    @TableField(typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> payload;
    private String status;
    private Instant scheduledAt;
    private Instant expiresAt;
    private Integer priority;
    private Instant createdAt;
    private Instant updatedAt;
}

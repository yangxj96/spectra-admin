package com.devops00.spectra.notification.preference.javabean.entity;

import java.time.Instant;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 用户用途×渠道通知偏好实体。 */
@Data
@TableName(value = "ntf_user_preference", schema = "spectra_notification")
public class NotificationUserPreferenceEntity {
    private UUID id;
    private UUID tenantId;
    private UUID userId;
    private String purpose;
    private String channel;
    private Boolean enabled;
    private Boolean doNotDisturb;
    private Instant createdAt;
    private Instant updatedAt;
}

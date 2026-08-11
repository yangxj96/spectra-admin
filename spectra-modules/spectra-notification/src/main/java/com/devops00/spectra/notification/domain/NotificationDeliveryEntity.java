package com.devops00.spectra.notification.domain;

import java.time.Instant;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 通知投递记录实体。 */
@Data
@TableName(value = "ntf_delivery", schema = "spectra_notification")
public class NotificationDeliveryEntity {
    private UUID id;
    private UUID taskId;
    private String providerCode;
    private String providerMessageId;
    private String status;
    private String responseSummary;
    private Instant sentAt;
    private Instant createdAt;
}

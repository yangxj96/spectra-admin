package com.devops00.spectra.core.notification.service;

import java.util.Map;
import java.util.UUID;

import com.devops00.spectra.core.notification.javabean.entity.NotificationRequest;

/** 通知请求服务，负责租户边界和幂等键。 */
public interface NotificationRequestService {

    /**
     * 创建或获取幂等通知请求。
     *
     * @return 已存在或新建的请求
     */
    NotificationRequest accept(UUID tenantId, String idempotencyKey, String businessType, String businessId,
            String templateCode, UUID senderUserId, String dataScopeKey, Map<String, Object> payload);
}

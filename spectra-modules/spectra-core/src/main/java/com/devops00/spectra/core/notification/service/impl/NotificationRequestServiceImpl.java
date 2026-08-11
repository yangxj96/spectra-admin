package com.devops00.spectra.core.notification.service.impl;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.core.notification.javabean.entity.NotificationRequest;
import com.devops00.spectra.core.notification.mapper.NotificationRequestMapper;
import com.devops00.spectra.core.notification.service.NotificationRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 通知请求服务实现。 */
@Service
@RequiredArgsConstructor
public class NotificationRequestServiceImpl implements NotificationRequestService {

    private final NotificationRequestMapper notificationRequestMapper;

    @Override
    @Transactional
    public NotificationRequest accept(UUID tenantId, String idempotencyKey, String businessType, String businessId,
            String templateCode, UUID senderUserId, String dataScopeKey, Map<String, Object> payload) {
        if (tenantId == null || !StringUtils.hasText(idempotencyKey) || !StringUtils.hasText(businessType)
                || !StringUtils.hasText(businessId) || !StringUtils.hasText(templateCode)) {
            throw new DataSaveException("通知请求参数不完整");
        }
        var wrapper = new LambdaQueryWrapper<NotificationRequest>().eq(NotificationRequest::getTenantId, tenantId)
                .eq(NotificationRequest::getIdempotencyKey, idempotencyKey);
        var existing = notificationRequestMapper.selectOne(wrapper);
        if (existing != null) {
            return existing;
        }
        var entity = new NotificationRequest();
        entity.setId(UUID.randomUUID());
        entity.setTenantId(tenantId);
        entity.setBusinessType(businessType);
        entity.setBusinessId(businessId);
        entity.setIdempotencyKey(idempotencyKey);
        entity.setTemplateCode(templateCode);
        entity.setSenderUserId(senderUserId);
        entity.setDataScopeKey(dataScopeKey);
        entity.setPayload(payload == null ? Map.of() : payload);
        entity.setStatus("ACCEPTED");
        entity.setCreatedAt(Instant.now());
        if (notificationRequestMapper.insert(entity) != 1) {
            throw new DataSaveException("创建通知请求失败");
        }
        return entity;
    }
}

package com.devops00.spectra.notification.preference;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.notification.domain.NotificationUserPreferenceEntity;
import com.devops00.spectra.notification.mapper.NotificationUserPreferenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 用户用途×渠道偏好服务实现。 */
@Service
@RequiredArgsConstructor
public class NotificationPreferenceServiceImpl implements NotificationPreferenceService {

    private static final List<String> MANDATORY_PURPOSES = List.of("LOGIN_CODE", "BIND_PHONE_CODE", "BIND_EMAIL_CODE",
            "RESET_PASSWORD_CODE", "SECURITY_ALERT");

    private final NotificationUserPreferenceMapper mapper;

    @Override
    public List<NotificationUserPreferenceEntity> list(UUID tenantId, UUID userId) {
        return mapper.selectList(new LambdaQueryWrapper<NotificationUserPreferenceEntity>().eq(NotificationUserPreferenceEntity::getTenantId, tenantId)
                .eq(NotificationUserPreferenceEntity::getUserId, userId).orderByAsc(NotificationUserPreferenceEntity::getPurpose)
                .orderByAsc(NotificationUserPreferenceEntity::getChannel));
    }

    @Override
    @Transactional
    public void save(UUID tenantId, UUID userId, String purpose, String channel, boolean enabled, boolean doNotDisturb) {
        if (tenantId == null || userId == null || !StringUtils.hasText(purpose) || !StringUtils.hasText(channel)) {
            throw new DataSaveException("通知偏好参数不完整");
        }
        var normalizedPurpose = purpose.toUpperCase();
        var normalizedChannel = channel.toUpperCase();
        if (MANDATORY_PURPOSES.contains(normalizedPurpose)) {
            enabled = true;
            doNotDisturb = false;
        }
        var query = new LambdaQueryWrapper<NotificationUserPreferenceEntity>().eq(NotificationUserPreferenceEntity::getTenantId, tenantId)
                .eq(NotificationUserPreferenceEntity::getUserId, userId).eq(NotificationUserPreferenceEntity::getPurpose, normalizedPurpose)
                .eq(NotificationUserPreferenceEntity::getChannel, normalizedChannel);
        var entity = mapper.selectOne(query);
        if (entity == null) {
            entity = new NotificationUserPreferenceEntity();
            entity.setId(UUID.randomUUID());
            entity.setTenantId(tenantId);
            entity.setUserId(userId);
            entity.setPurpose(normalizedPurpose);
            entity.setChannel(normalizedChannel);
            entity.setCreatedAt(Instant.now());
        }
        entity.setEnabled(enabled);
        entity.setDoNotDisturb(doNotDisturb);
        entity.setUpdatedAt(Instant.now());
        if (entity.getId() != null && mapper.selectById(entity.getId()) == null ? mapper.insert(entity) != 1 : mapper.updateById(entity) != 1) {
            throw new DataSaveException("保存通知偏好失败");
        }
    }
}

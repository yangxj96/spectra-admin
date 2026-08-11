package com.devops00.spectra.notification.preference;

import java.util.List;
import java.util.UUID;

import com.devops00.spectra.notification.domain.NotificationUserPreferenceEntity;

/** 用户用途×渠道偏好服务。 */
public interface NotificationPreferenceService {

    /** 查询用户偏好。 */
    List<NotificationUserPreferenceEntity> list(UUID tenantId, UUID userId);

    /** 保存可选用途偏好；安全用途由调用策略强制开启。 */
    void save(UUID tenantId, UUID userId, String purpose, String channel, boolean enabled, boolean doNotDisturb);
}

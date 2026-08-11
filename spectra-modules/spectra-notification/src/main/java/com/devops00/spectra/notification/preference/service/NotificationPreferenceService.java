package com.devops00.spectra.notification.preference.service;

import java.util.List;
import java.util.UUID;

import com.devops00.spectra.notification.preference.javabean.entity.NotificationUserPreferenceEntity;
import com.devops00.spectra.notification.preference.javabean.from.NotificationSettingFrom;
import com.devops00.spectra.notification.preference.javabean.vo.NotificationSettingVO;

/** 用户用途×渠道偏好服务。 */
public interface NotificationPreferenceService {

    /** 查询用户偏好。 */
    List<NotificationUserPreferenceEntity> list(UUID tenantId, UUID userId);

    /** 保存可选用途偏好；安全用途由调用策略强制开启。 */
    void save(UUID tenantId, UUID userId, String purpose, String channel, boolean enabled, boolean doNotDisturb);

    /** 读取旧消息中心设置结构，供兼容 API 使用。 */
    NotificationSettingVO legacy(UUID tenantId, UUID userId);

    /** 保存旧消息中心设置结构，内部展开为用途×渠道记录。 */
    void saveLegacy(UUID tenantId, UUID userId, NotificationSettingFrom from);
}

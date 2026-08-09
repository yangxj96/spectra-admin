package com.devops00.spectra.core.notification.service;

import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.core.notification.javabean.entity.NotificationSetting;
import com.devops00.spectra.core.notification.javabean.from.NotificationSettingFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationSettingVO;

import java.util.UUID;

/**
 * 通知设置Service
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/19
 */
public interface NotificationSettingService extends BaseService<NotificationSetting> {

    /**
     * 获取用户消息设置
     */
    NotificationSettingVO getSetting(UUID userId);

    /**
     * 更新消息设置
     */
    void updateSetting(UUID userId, NotificationSettingFrom from);
}

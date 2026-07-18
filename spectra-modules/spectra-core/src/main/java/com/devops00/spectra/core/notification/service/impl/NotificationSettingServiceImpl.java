package com.devops00.spectra.core.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.core.notification.javabean.converter.NotificationSettingConverter;
import com.devops00.spectra.core.notification.javabean.entity.NotificationSetting;
import com.devops00.spectra.core.notification.javabean.from.NotificationSettingFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationSettingVO;
import com.devops00.spectra.core.notification.mapper.NotificationSettingMapper;
import com.devops00.spectra.core.notification.service.NotificationSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.UUID;

/// 通知设置Service实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/19
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSettingServiceImpl
        extends BaseServiceImpl<NotificationSettingMapper, NotificationSetting>
        implements NotificationSettingService {

    private final NotificationSettingConverter notificationSettingConverter;

    @Override
    public NotificationSettingVO getSetting(UUID userId) {
        var wrapper = new LambdaQueryWrapper<NotificationSetting>();
        wrapper.eq(NotificationSetting::getUserId, userId);
        var entity = this.getOne(wrapper);

        if (entity == null) {
            // 创建默认设置
            entity = createDefaultSetting(userId);
        }

        return notificationSettingConverter.toVO(entity);
    }

    @Override
    @Transactional
    public void updateSetting(UUID userId, NotificationSettingFrom from) {
        var wrapper = new LambdaQueryWrapper<NotificationSetting>();
        wrapper.eq(NotificationSetting::getUserId, userId);
        var entity = this.getOne(wrapper);

        if (entity == null) {
            entity = createDefaultSetting(userId);
        }

        notificationSettingConverter.updateEntity(from, entity);

        // 解析时间字符串
        if (from.getDoNotDisturbStart() != null && !from.getDoNotDisturbStart().isEmpty()) {
            entity.setDoNotDisturbStart(LocalTime.parse(from.getDoNotDisturbStart()));
        }
        if (from.getDoNotDisturbEnd() != null && !from.getDoNotDisturbEnd().isEmpty()) {
            entity.setDoNotDisturbEnd(LocalTime.parse(from.getDoNotDisturbEnd()));
        }

        if (!this.updateById(entity)) {
            throw new DataSaveException("更新消息设置失败");
        }
        log.info("更新消息设置成功: userId={}", userId);
    }

    /// 创建默认设置
    private NotificationSetting createDefaultSetting(UUID userId) {
        var entity = new NotificationSetting();
        entity.setUserId(userId);
        entity.setSystemEnabled(true);
        entity.setWorkflowEnabled(true);
        entity.setOaEnabled(true);
        entity.setInnerMailEnabled(true);
        entity.setApprovalEnabled(true);
        entity.setDoNotDisturb(false);

        if (!this.save(entity)) {
            throw new DataSaveException("创建默认消息设置失败");
        }
        log.info("创建默认消息设置: userId={}", userId);
        return entity;
    }
}

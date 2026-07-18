package com.devops00.spectra.core.notification.javabean.converter;

import com.devops00.spectra.core.notification.javabean.entity.NotificationSetting;
import com.devops00.spectra.core.notification.javabean.from.NotificationSettingFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationSettingVO;
import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

/// 通知设置mapstruct
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/19
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface NotificationSettingConverter {

    /// 实体转VO
    NotificationSettingVO toVO(NotificationSetting source);

    /// 使用From更新实体
    void updateEntity(NotificationSettingFrom source, @MappingTarget NotificationSetting target);
}

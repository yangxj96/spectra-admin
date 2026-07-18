package com.devops00.spectra.core.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.notification.javabean.entity.NotificationSetting;
import org.apache.ibatis.annotations.Mapper;

/// 通知设置Mapper
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/19
@Mapper
public interface NotificationSettingMapper extends BaseMapper<NotificationSetting> {
}

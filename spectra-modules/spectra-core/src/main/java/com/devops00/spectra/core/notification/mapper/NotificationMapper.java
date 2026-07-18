package com.devops00.spectra.core.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.notification.javabean.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/// 消息Mapper
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/19
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}

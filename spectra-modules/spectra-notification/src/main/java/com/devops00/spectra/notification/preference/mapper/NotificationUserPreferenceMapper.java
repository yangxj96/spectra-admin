package com.devops00.spectra.notification.preference.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.notification.preference.javabean.entity.NotificationUserPreferenceEntity;
import org.apache.ibatis.annotations.Mapper;

/** 用户通知偏好 Mapper。 */
@Mapper
public interface NotificationUserPreferenceMapper extends BaseMapper<NotificationUserPreferenceEntity> {
}

package com.devops00.spectra.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.notification.domain.NotificationUserPreferenceEntity;
import org.apache.ibatis.annotations.Mapper;

/** 用户通知偏好 Mapper。 */
@Mapper
public interface NotificationUserPreferenceMapper extends BaseMapper<NotificationUserPreferenceEntity> {
}

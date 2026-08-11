package com.devops00.spectra.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.notification.domain.NotificationRequestEntity;
import org.apache.ibatis.annotations.Mapper;

/** 通知请求 Mapper。 */
@Mapper
public interface NotificationRequestMapper extends BaseMapper<NotificationRequestEntity> {
}

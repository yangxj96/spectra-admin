package com.devops00.spectra.notification.dispatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationDeliveryEntity;
import org.apache.ibatis.annotations.Mapper;

/** 通知投递 Mapper。 */
@Mapper
public interface NotificationDeliveryMapper extends BaseMapper<NotificationDeliveryEntity> {
}

package com.devops00.spectra.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.notification.domain.NotificationDeliveryEntity;
import org.apache.ibatis.annotations.Mapper;

/** 通知投递 Mapper。 */
@Mapper
public interface NotificationDeliveryMapper extends BaseMapper<NotificationDeliveryEntity> {
}

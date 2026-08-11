package com.devops00.spectra.core.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.notification.javabean.entity.NotificationDelivery;
import org.apache.ibatis.annotations.Mapper;

/** 通知投递记录 Mapper。 */
@Mapper
public interface NotificationDeliveryMapper extends BaseMapper<NotificationDelivery> {
}

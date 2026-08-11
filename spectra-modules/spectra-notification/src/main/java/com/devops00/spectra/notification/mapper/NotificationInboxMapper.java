package com.devops00.spectra.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.notification.domain.NotificationInboxEntity;
import org.apache.ibatis.annotations.Mapper;

/** 站内信 Mapper。 */
@Mapper
public interface NotificationInboxMapper extends BaseMapper<NotificationInboxEntity> {
}

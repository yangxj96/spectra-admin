package com.devops00.spectra.notification.inbox.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.notification.inbox.javabean.entity.NotificationInboxEntity;
import org.apache.ibatis.annotations.Mapper;

/** 站内信 Mapper。 */
@Mapper
public interface NotificationInboxMapper extends BaseMapper<NotificationInboxEntity> {
}

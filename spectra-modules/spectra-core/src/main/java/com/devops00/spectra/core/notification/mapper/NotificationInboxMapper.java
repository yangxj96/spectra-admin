package com.devops00.spectra.core.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.notification.javabean.entity.NotificationInbox;
import org.apache.ibatis.annotations.Mapper;

/** 站内信收件箱 Mapper。 */
@Mapper
public interface NotificationInboxMapper extends BaseMapper<NotificationInbox> {
}

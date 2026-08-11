package com.devops00.spectra.notification.dispatch.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.notification.dispatch.javabean.entity.NotificationTaskEntity;
import org.apache.ibatis.annotations.Mapper;

/** 通知任务 Mapper。 */
@Mapper
public interface NotificationTaskMapper extends BaseMapper<NotificationTaskEntity> {
}

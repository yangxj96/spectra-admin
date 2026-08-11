package com.devops00.spectra.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.notification.domain.NotificationTaskEntity;
import org.apache.ibatis.annotations.Mapper;

/** 通知任务 Mapper。 */
@Mapper
public interface NotificationTaskMapper extends BaseMapper<NotificationTaskEntity> {
}

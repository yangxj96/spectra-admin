/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.system.javabean.entity.ServiceMonitorAlertEvent;
import org.apache.ibatis.annotations.Mapper;

/** 服务监控告警事件 Mapper。 */
@Mapper
public interface ServiceMonitorAlertEventMapper extends BaseMapper<ServiceMonitorAlertEvent> {
}

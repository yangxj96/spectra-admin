/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.system.javabean.entity.ServiceMonitorDiagnosticTask;
import org.apache.ibatis.annotations.Mapper;

/** 服务监控诊断任务 Mapper。 */
@Mapper
public interface ServiceMonitorDiagnosticTaskMapper extends BaseMapper<ServiceMonitorDiagnosticTask> {
}

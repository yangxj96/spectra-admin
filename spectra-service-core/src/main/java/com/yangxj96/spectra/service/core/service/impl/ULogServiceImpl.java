package com.yangxj96.spectra.service.core.service.impl;

import com.yangxj96.spectra.service.core.javabean.entity.OperationLog;
import com.yangxj96.spectra.service.core.service.OperationLogService;
import com.yangxj96.spectra.starter.common.entity.ULogEntity;
import com.yangxj96.spectra.starter.common.service.ULogService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 日志记录Service
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/27
 */
@Service
public class ULogServiceImpl implements ULogService {

    @Resource
    private OperationLogService operationLogService;

    @Override
    @Async("uLogTaskExecutor")
    public void save(ULogEntity entity) {
        OperationLog log = new OperationLog();
        BeanUtils.copyProperties(entity, log);
        operationLogService.save(log);
    }
}

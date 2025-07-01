package com.yangxj96.spectra.service.core.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.yangxj96.spectra.common.constant.Common;
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
    public void save(ULogEntity entity, String token) {
        var id = Common.PID;
        try {
            id = Long.parseLong(StpUtil.getLoginIdByToken(token).toString());
        } catch (Exception ignored) {
            // 有可能出错导致转换失败,但是id已经设置了默认值,所以不用处理异常
        }
        OperationLog log = new OperationLog();
        BeanUtils.copyProperties(entity, log);
        log.setCreatedBy(id);
        log.setUpdatedBy(id);
        operationLogService.save(log);
    }
}

package com.yangxj96.spectra.core.system.configure;

import cn.dev33.satoken.stp.StpUtil;
import com.yangxj96.spectra.common.javabean.ULogEntity;
import com.yangxj96.spectra.core.system.javabean.entity.OperationLog;
import com.yangxj96.spectra.core.system.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 日志消息监听器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/3
 */
@Slf4j
@Component
public class ULogListener {

    private final OperationLogService logService;

    public ULogListener(OperationLogService logService) {
        this.logService = logService;
    }

    @Async("uLogTaskExecutor")
    @EventListener
    public void handleLogEvent(ULogEntity entity) {
        log.atDebug().log("日志消息监听器,开始记录");
        OperationLog datum = new OperationLog();
        BeanUtils.copyProperties(entity, datum);
        if (StringUtils.isNotBlank(entity.getToken())) {
            try {
                Object loginId = StpUtil.getLoginIdByToken(entity.getToken());
                datum.setCreatedBy(Long.parseLong(loginId.toString()));
                datum.setUpdatedBy(Long.parseLong(loginId.toString()));
            } catch (Exception e) {
                log.atError().log("获取登录用户ID失败", e);
            }
        }
        logService.save(datum);
    }

}

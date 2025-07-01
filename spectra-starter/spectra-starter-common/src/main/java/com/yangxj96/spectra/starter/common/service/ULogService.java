package com.yangxj96.spectra.starter.common.service;


import com.yangxj96.spectra.starter.common.entity.ULogEntity;

/**
 * ULog注解AOP拦截后操作Service层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-06-27
 */
public interface ULogService {

    /**
     * 保存日志
     *
     * @param entity 日志信息实体
     */
    void save(ULogEntity entity,String token);
}

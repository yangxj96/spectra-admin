package com.yangxj96.spectra.starter.common.service;


import com.yangxj96.spectra.starter.common.entity.ULogEntity;

public interface ULogService {

    /**
     * 保存日志
     *
     * @param entity 日志信息实体
     * @return 是否保存成功
     */
    boolean save(ULogEntity entity);
}

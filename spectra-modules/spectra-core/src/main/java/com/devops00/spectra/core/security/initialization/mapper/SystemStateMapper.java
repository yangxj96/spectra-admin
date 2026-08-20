/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.initialization.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.security.initialization.javabean.entity.SystemState;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 系统初始化状态持久化接口。 */
@Mapper
public interface SystemStateMapper extends BaseMapper<SystemState> {

    /**
     * 按状态键锁定系统初始化状态行。
     *
     * <p>使用显式 SQL，避免实体基类的默认排序被拼接到 {@code FOR UPDATE} 之后。</p>
     *
     * @param stateKey 状态键
     * @return 系统初始化状态
     */
    SystemState selectForUpdateByStateKey(@Param("stateKey") String stateKey);
}

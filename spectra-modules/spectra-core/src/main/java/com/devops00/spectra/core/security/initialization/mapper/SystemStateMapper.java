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

/** 系统初始化状态持久化接口。 */
@Mapper
public interface SystemStateMapper extends BaseMapper<SystemState> {
}

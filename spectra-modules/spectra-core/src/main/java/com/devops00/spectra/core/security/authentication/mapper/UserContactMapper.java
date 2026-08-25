/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.authentication.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.security.authentication.javabean.entity.UserContact;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserContactMapper extends BaseMapper<UserContact> {
}

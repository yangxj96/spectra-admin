/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication.mfa.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.security.authentication.mfa.entity.MfaEnrollment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MfaEnrollmentMapper extends BaseMapper<MfaEnrollment> {
}

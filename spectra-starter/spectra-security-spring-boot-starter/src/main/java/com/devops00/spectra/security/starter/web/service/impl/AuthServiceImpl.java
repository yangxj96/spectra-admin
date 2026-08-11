/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.security.starter.web.service.impl;

import com.devops00.spectra.common.exception.SpectraException;
import com.devops00.spectra.security.starter.web.service.AuthService;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/6/28
 */
@Service
@NullMarked
public class AuthServiceImpl implements AuthService {

    @Override
    public void sendSmsCode(String phone) {
        throw new SpectraException("短信验证码服务暂未启用");
    }

    @Override
    public void sendEmailCode(String email) {
        throw new SpectraException("邮箱验证码服务暂未启用");
    }
}

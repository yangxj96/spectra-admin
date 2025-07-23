/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.core.auth.service;


import com.yangxj96.spectra.core.auth.javabean.from.UsernamePasswordFrom;
import com.yangxj96.spectra.core.auth.javabean.vo.TokenVO;

import javax.security.auth.login.LoginException;

/**
 * 认证service层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param params 账号密码登录参数
     * @return 成功则响应token
     */
    TokenVO login(UsernamePasswordFrom params) throws LoginException;

    /**
     * 用户退出
     */
    void logout();
}

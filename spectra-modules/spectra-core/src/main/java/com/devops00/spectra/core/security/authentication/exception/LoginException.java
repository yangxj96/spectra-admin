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

package com.devops00.spectra.core.security.authentication.exception;

import org.springframework.security.authentication.BadCredentialsException;

/**
 * 登录异常。
 *
 * <p>本类保留 Spring Security 要求的 BadCredentialsException 父类，以便参与认证失败处理流程；
 * 这是已审核的框架契约例外，不属于项目自定义 RuntimeException 平行根类。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/2/19 23:14
 */
public class LoginException extends BadCredentialsException {

    public LoginException(String message) {
        super(message);
    }
}

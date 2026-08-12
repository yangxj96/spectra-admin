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

package com.devops00.spectra.security.starter.web.advice;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.response.R;
import com.devops00.spectra.security.base.exception.LoginException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 业务异常拦截
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/2/19 23:24
 */
@Slf4j
@Order(-50)
@RestControllerAdvice
public class LoginExceptionAdvice {

    /**
     * 登陆失败拦截
     *
     * @param e        [LoginException]错误信息
     * @param response [HttpServletResponse]响应对象
     * @return 通用响应对象
     */
    @ExceptionHandler(LoginException.class)
    public R<Object> handleLoginException(LoginException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        log.warn("{}登录失败: {}", LogPrefix.SECURITY.p(), e.getMessage(), e);
        return R.failure(HttpStatus.UNAUTHORIZED, e.getMessage());
    }
}

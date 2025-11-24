/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.framework.advice.exception;

import io.github.yangxj96.spectra.common.exception.KaptchaExpiresException;
import io.github.yangxj96.spectra.common.exception.KaptchaNotMatchException;
import io.github.yangxj96.spectra.common.response.R;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 验证码相关错误处理
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/28
 */
@Slf4j
@NullMarked
@Order(Integer.MIN_VALUE)
@RestControllerAdvice
public class KaptchaExceptionAdvice {

    private static final String PREFIX = "[验证码错误处理]:";

    /**
     * 验证码不匹配
     *
     * @param e        错误信息
     * @param response 响应
     * @return 格式化为正常响应返回
     */
    @ExceptionHandler(KaptchaNotMatchException.class)
    public R<Object> kaptchaNotMatchException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.error(PREFIX + "验证码不匹配,{}", e.getMessage(), e);
        return R.failure("验证码不匹配");
    }


    /**
     * 验证码过期
     *
     * @param e        错误信息
     * @param response 响应
     * @return 格式化为正常响应返回
     */
    @ExceptionHandler(KaptchaExpiresException.class)
    public R<Object> kaptchaExpiresException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.error(PREFIX + "验证码过期,{}", e.getMessage(), e);
        return R.failure("验证码过期");
    }

}

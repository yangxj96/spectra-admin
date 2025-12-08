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

package io.github.yangxj96.spectra.core.configure.mvc.advice.exception;

import io.github.yangxj96.spectra.common.response.R;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 权限错误相关处理
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/28
 */
@Slf4j
@NullMarked
@Order(Integer.MIN_VALUE)
@RestControllerAdvice
public class AuthExceptionAdvice {

    private static final String PREFIX = "[权限错误相关处理]:";

    /**
     * 无权限异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 格式化为正常响应返回
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public R<Object> notPermissionException(AuthorizationDeniedException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        log.error(PREFIX + "无权限异常,{}", e.getMessage(), e);
        return R.failure(HttpStatus.UNAUTHORIZED, "无权操作");
    }

}

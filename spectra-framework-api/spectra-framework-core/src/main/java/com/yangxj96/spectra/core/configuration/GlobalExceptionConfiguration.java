/*
 * Copyright (c) 2018-2025 Jack Young (杨新杰)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.yangxj96.spectra.core.configuration;

import cn.dev33.satoken.error.SaErrorCode;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.yangxj96.spectra.core.annotation.ULog;
import com.yangxj96.spectra.core.exception.DataExistException;
import com.yangxj96.spectra.core.exception.DataNotExistException;
import com.yangxj96.spectra.core.response.R;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import javax.security.auth.login.LoginException;

/**
 * 全局异常处理
 *
 * @author 杨新杰
 * @since 2025/5/26 17:07
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionConfiguration {


    /**
     * 未找到资源
     *
     * @param e        错误信息
     * @param response 响应
     * @return 格式化为正常的响应返回
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public R<Object> noResourceFoundException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.atError().log("未找到资源,{}", e.getMessage(), e);
        return R.failure("未找到资源");
    }

    /**
     * 无权限异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 格式化为正常的响应返回
     */
    @ULog("无权操作")
    @ExceptionHandler(NotPermissionException.class)
    public R<Object> notPermissionException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        log.atError().log("无权限异常,{}", e.getMessage(), e);
        return R.failure(HttpStatus.FORBIDDEN, "无权操作");
    }

    /**
     * 未登录异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 格式化为正常的响应返回
     */
    @ExceptionHandler(NotLoginException.class)
    public R<Object> notLoginException(NotLoginException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        log.atError().log("未登录异常,{}", e.getMessage(), e);
        if (e.getCode() == SaErrorCode.CODE_11016) {
            return R.failure(HttpStatus.UNAUTHORIZED, "您的会话已过期，请重新登录以继续。");
        }
        return R.failure(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    /**
     * 登录异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 格式化为正常的响应返回
     */
    @ExceptionHandler(LoginException.class)
    public R<Object> loginException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.atError().log("登录异常,{}", e.getMessage(), e);
        return R.failure(e.getMessage());
    }

    /**
     * 未进行功能实现异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 格式化为正常的响应返回
     */
    @ExceptionHandler(NotImplementedException.class)
    public R<Object> notImplementedException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.atError().log("未进行功能实现异常,{}", e.getMessage(), e);
        return R.failure();
    }

    /**
     * 数据已存在异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 格式化为正常的响应返回
     */
    @ExceptionHandler(DataExistException.class)
    public R<Object> dataExistException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.CONFLICT.value());
        log.atError().log("数据已存在异常,{}", e.getMessage(), e);
        return R.failure(HttpStatus.CONFLICT);
    }

    /**
     * 数据不存在异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 格式化为正常的响应返回
     */
    @ExceptionHandler(DataNotExistException.class)
    public R<Object> dataNotExistException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        log.atError().log("数据不存在异常,{} ", e.getMessage(), e);
        return R.failure(HttpStatus.NOT_FOUND);
    }

    /**
     * 参数验证异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 格式化为正常的响应返回
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Object> methodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletResponse response) {
        log.atError().log("参数验证异常,{} ", e.getMessage(), e);
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        var errors = e.getBindingResult().getAllErrors();
        if (!errors.isEmpty()) {
            return R.failure(HttpStatus.BAD_REQUEST, errors.get(0).getDefaultMessage());
        } else {
            return R.failure(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 兜底异常处理
     *
     * @param e        错误信息
     * @param response 响应
     * @return 格式化为正常的响应返回
     */
    @ExceptionHandler(Exception.class)
    public R<Object> handleException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.atError().log("兜底异常处理,{}", e.getMessage(), e);
        return R.failure();
    }

}

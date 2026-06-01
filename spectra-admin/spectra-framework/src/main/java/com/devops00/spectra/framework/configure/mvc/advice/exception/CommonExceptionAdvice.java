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

package com.devops00.spectra.framework.configure.mvc.advice.exception;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.exception.DataExistException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.NotImplementedException;
import com.devops00.spectra.common.response.R;
import com.devops00.spectra.common.utils.StrUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/// 通用异常处理
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-6-14
@Slf4j
@Order
@NullMarked
@RestControllerAdvice
public class CommonExceptionAdvice {

    /// 未找到资源
    ///
    /// @param e        错误信息
    /// @param response 响应
    /// @return 格式化为正常响应返回
    @ExceptionHandler(NoResourceFoundException.class)
    public R<Object> noResourceFoundException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.error("{}未找到资源,{}", LogPrefix.WEB.p(), e.getMessage(), e);
        return R.failure("未找到资源");
    }


    /// 未进行功能实现异常
    ///
    /// @param e        错误信息
    /// @param response 响应
    /// @return 格式化为正常响应返回
    @ExceptionHandler(NotImplementedException.class)
    public R<Object> notImplementedException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.error("{}未进行功能实现异常,{}", LogPrefix.WEB.p(), e.getMessage(), e);
        return R.failure("功能暂未实现");
    }

    /// 数据已存在异常
    ///
    /// @param e        错误信息
    /// @param response 响应
    /// @return 格式化为正常响应返回
    @ExceptionHandler(DataExistException.class)
    public R<Object> dataExistException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.CONFLICT.value());
        log.error("{}数据已存在异常,{}", LogPrefix.WEB.p(), e.getMessage(), e);
        return R.failure(HttpStatus.CONFLICT);
    }

    /// 数据不存在异常
    ///
    /// @param e        错误信息
    /// @param response 响应
    /// @return 格式化为正常响应返回
    @ExceptionHandler(DataNotExistException.class)
    public R<Object> dataNotExistException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        log.error("{}数据不存在异常,{} ", LogPrefix.WEB.p(), e.getMessage(), e);
        return R.failure(HttpStatus.NOT_FOUND);
    }

    /// 参数验证异常
    ///
    /// @param e        错误信息
    /// @param response 响应
    /// @return 格式化为正常响应返回
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Object> methodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletResponse response) {
        log.error("{}参数验证异常,{} ", LogPrefix.WEB.p(), e.getMessage(), e);
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        var errors = e.getBindingResult().getAllErrors();
        if (!errors.isEmpty()) {
            String message = errors.getFirst().getDefaultMessage();
            if (message == null || StrUtils.isEmpty(message)) {
                message = "参数验证一场";
            }
            return R.failure(HttpStatus.BAD_REQUEST, message);
        } else {
            return R.failure(HttpStatus.BAD_REQUEST);
        }
    }

    /// 运行时异常
    ///
    /// @param e        错误信息
    /// @param response 响应
    /// @return 格式化为正常响应返回
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Object> httpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.error("{}JSON 反序列化失败: {}", LogPrefix.WEB.p(), e.getMessage(), e);
        return R.failure("请求数据格式错误，请检查JSON格式和字段类型");
    }

    /// 运行时异常
    ///
    /// @param e        错误信息
    /// @param response 响应
    /// @return 格式化为正常响应返回
    @ExceptionHandler(RuntimeException.class)
    public R<Object> runtimeException(RuntimeException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.error("{}运行时异常,{}", LogPrefix.WEB.p(), e.getMessage(), e);
        return R.failure(e.getMessage());
    }

    /// 兜底异常处理
    ///
    /// @param e        错误信息
    /// @param response 响应
    /// @return 格式化为正常响应返回
    @ExceptionHandler(Exception.class)
    public R<Object> handleException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.error("{}兜底异常处理,{}", LogPrefix.WEB.p(), e.getMessage(), e);
        return R.failure("系统内部错误,请联系管理员");
    }

}

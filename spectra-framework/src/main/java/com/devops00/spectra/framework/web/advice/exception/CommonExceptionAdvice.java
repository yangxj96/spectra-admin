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

package com.devops00.spectra.framework.web.advice.exception;

import com.devops00.spectra.common.audit.RequestCorrelationContext;
import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.exception.DataExistException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataScopeViolationException;
import com.devops00.spectra.common.exception.NotImplementedException;
import com.devops00.spectra.common.response.R;
import com.devops00.spectra.common.foundation.lang.StrUtils;
import com.devops00.spectra.framework.web.advice.crypto.RequestCryptoException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 通用异常处理
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Slf4j
@Order(-10)
@NullMarked
@RestControllerAdvice
public class CommonExceptionAdvice {

    /**
     * 方法级权限不足异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 返回 HTTP 403 的统一失败响应，消息为“权限不足”；响应对象始终非 null。
     */
    @ExceptionHandler(AccessDeniedException.class)
    public R<Object> accessDeniedException(AccessDeniedException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        log.warn("{}权限不足,{}", LogPrefix.WEB.p(), e.getMessage());
        return R.failure(HttpStatus.FORBIDDEN, "权限不足");
    }

    /**
     * 数据范围缺失或越权异常，统一返回 403，避免被兜底处理成 500。
     *
     * @param e        待转换为统一响应的异常对象。
     * @param response 当前 HTTP 响应，用于写入状态、响应头和统一响应体。
     * @return 返回 HTTP 403 的统一失败响应，消息为“数据范围不足”；响应对象始终非 null。
     */
    @ExceptionHandler(DataScopeViolationException.class)
    public R<Object> dataScopeViolationException(DataScopeViolationException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        log.warn("{}数据范围校验失败,{}", LogPrefix.WEB.p(), e.getMessage());
        return R.failure(HttpStatus.FORBIDDEN, "数据范围不足");
    }

    /**
     * 未找到资源
     *
     * @param e        错误信息
     * @param response 响应
     * @return 返回 HTTP 500 的“未找到资源”统一失败响应；响应对象始终非 null。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public R<Object> noResourceFoundException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.error("{}未找到资源,{}", LogPrefix.WEB.p(), e.getMessage(), e);
        return R.failure("未找到资源");
    }

    /**
     * 未进行功能实现异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 返回 HTTP 500 的“功能暂未实现”统一失败响应；响应对象始终非 null。
     */
    @ExceptionHandler(NotImplementedException.class)
    public R<Object> notImplementedException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.error("{}未进行功能实现异常,{}", LogPrefix.WEB.p(), e.getMessage(), e);
        return R.failure("功能暂未实现");
    }

    /**
     * 数据已存在异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 返回 HTTP 409 的数据冲突统一失败响应；响应对象始终非 null。
     */
    @ExceptionHandler(DataExistException.class)
    public R<Object> dataExistException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.CONFLICT.value());
        log.error("{}数据已存在异常,{}", LogPrefix.WEB.p(), e.getMessage(), e);
        return R.failure(HttpStatus.CONFLICT, e.getMessage());
    }

    /**
     * 数据不存在异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 返回 HTTP 404 的数据不存在统一失败响应；响应对象始终非 null。
     */
    @ExceptionHandler(DataNotExistException.class)
    public R<Object> dataNotExistException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        log.error("{}数据不存在异常,{} ", LogPrefix.WEB.p(), e.getMessage(), e);
        return R.failure(HttpStatus.NOT_FOUND);
    }

    /**
     * 参数验证异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 返回 HTTP 400 的参数校验统一失败响应，优先携带第一个校验错误消息；响应对象始终非 null。
     */
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

    /**
     * 客户端加密请求格式错误统一返回 400，不暴露密文、签名或底层密码异常。
     *
     * @param e        待转换为统一响应的异常对象。
     * @param response 当前 HTTP 响应，用于写入状态、响应头和统一响应体。
     * @return 返回 HTTP 400 的请求加密数据无效统一失败响应，不包含密文或密钥；响应对象始终非 null。
     */
    @ExceptionHandler(RequestCryptoException.class)
    public R<Object> requestCryptoException(RequestCryptoException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        log.warn("{}请求加密校验失败，correlationId={}", LogPrefix.WEB.p(), correlationId());
        return R.failure(HttpStatus.BAD_REQUEST, "请求加密数据无效");
    }

    /**
     * 运行时异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 返回 HTTP 400 的请求体格式错误统一失败响应；响应对象始终非 null。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Object> httpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        log.warn("{}请求体不可读，correlationId={}", LogPrefix.WEB.p(), correlationId());
        return R.failure(HttpStatus.BAD_REQUEST, "请求数据格式错误，请检查请求体");
    }

    /**
     * 运行时异常
     *
     * @param e        错误信息
     * @param response 响应
     * @return 返回 HTTP 500 的通用系统内部错误统一失败响应；响应对象始终非 null且不暴露异常细节。
     */
    @ExceptionHandler(RuntimeException.class)
    public R<Object> runtimeException(RuntimeException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.error("{}运行时异常，correlationId={}", LogPrefix.WEB.p(), correlationId(), e);
        return R.failure("系统内部错误,请联系管理员");
    }

    /**
     * 兜底异常处理
     *
     * @param e        错误信息
     * @param response 响应
     * @return 返回 HTTP 500 的兜底系统内部错误统一失败响应；响应对象始终非 null且不暴露异常细节。
     */
    @ExceptionHandler(Exception.class)
    public R<Object> handleException(Exception e, HttpServletResponse response) {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        log.error("{}兜底异常处理，correlationId={}", LogPrefix.WEB.p(), correlationId(), e);
        return R.failure("系统内部错误,请联系管理员");
    }

    private static String correlationId() {
        String correlationId = RequestCorrelationContext.current().correlationId();
        return correlationId == null ? "unknown" : correlationId;
    }
}

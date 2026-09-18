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
import com.devops00.spectra.common.exception.BusinessRuleViolationException;
import com.devops00.spectra.common.exception.DataExistException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataScopeViolationException;
import com.devops00.spectra.common.exception.NotImplementedException;
import com.devops00.spectra.common.exception.SpectraException;
import com.devops00.spectra.framework.web.response.R;
import com.devops00.spectra.common.foundation.lang.StrUtils;
import com.devops00.spectra.framework.web.advice.crypto.RequestCryptoException;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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

    /** 负责把不同异常类型转换为统一的 HTTP 状态、消息和日志策略。 */
    private final ExceptionResponseResolver exceptionResponseResolver = new ExceptionResponseResolver();

    /**
     * 处理访问异常相关数据。
     */
    @ExceptionHandler(AccessDeniedException.class)
    public R<Object> accessDeniedException(AccessDeniedException e, HttpServletResponse response) {
        return writeResolved(e, response);
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
        return writeResolved(e, response);
    }

    /**
     * 处理异常相关数据。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public R<Object> noResourceFoundException(NoResourceFoundException e, HttpServletResponse response) {
        return writeResolved(e, response);
    }

    /**
     * 返回暂未实现功能的明确错误，避免被通用未知异常处理器改写成内部错误。
     */
    @ExceptionHandler(NotImplementedException.class)
    public R<Object> notImplementedException(NotImplementedException e, HttpServletResponse response) {
        return writeResolved(e, response);
    }

    /**
     * 处理异常相关数据。
     */
    @ExceptionHandler(DataExistException.class)
    public R<Object> dataExistException(DataExistException e, HttpServletResponse response) {
        return writeResolved(e, response);
    }

    /**
     * 将可恢复的业务规则校验失败返回为客户端错误。
     *
     * @param e        业务规则异常，包含面向用户的校验说明。
     * @param response 当前 HTTP 响应，用于设置请求错误状态。
     * @return HTTP 400 统一失败响应，并保留可展示的规则说明。
     */
    @ExceptionHandler(BusinessRuleViolationException.class)
    public R<Object> businessRuleViolationException(BusinessRuleViolationException e, HttpServletResponse response) {
        return writeResolved(e, response);
    }

    /**
     * 处理不异常相关数据。
     */
    @ExceptionHandler(DataNotExistException.class)
    public R<Object> dataNotExistException(DataNotExistException e, HttpServletResponse response) {
        return writeResolved(e, response);
    }

    /**
     * 将 Bean Validation 的第一条可展示消息返回给调用方。
     *
     * @param e        参数校验异常
     * @param response 当前 HTTP 响应
     * @return HTTP 400 的统一失败响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Object> methodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletResponse response) {
        log.error("{}参数验证异常,{} ", LogPrefix.WEB.p(), e.getMessage(), e);
        response.setStatus(HttpStatus.BAD_REQUEST.value());

        var errors = e.getBindingResult().getAllErrors();
        if (!errors.isEmpty()) {
            String message = errors.getFirst().getDefaultMessage();
            if (message == null || StrUtils.isEmpty(message)) {
                message = "参数验证异常";
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
     * 处理HTTP消息不异常相关数据。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Object> httpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        log.warn("{}请求体不可读，correlationId={}", LogPrefix.WEB.p(), correlationId());
        return R.failure(HttpStatus.BAD_REQUEST, "请求数据格式错误，请检查请求体");
    }

    /**
     * 处理项目自定义异常，避免它们落入未知运行时异常兜底。
     */
    @ExceptionHandler(SpectraException.class)
    public R<Object> spectraException(SpectraException e, HttpServletResponse response) {
        return writeResolved(e, response);
    }

    /**
     * 处理已审核的应用层参数异常。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public R<Object> illegalArgumentException(IllegalArgumentException e, HttpServletResponse response) {
        return writeResolved(e, response);
    }

    /**
     * 处理已审核的应用层状态异常。
     */
    @ExceptionHandler(IllegalStateException.class)
    public R<Object> illegalStateException(IllegalStateException e, HttpServletResponse response) {
        return writeResolved(e, response);
    }

    /**
     * 处理未被更具体处理器接收的运行时异常，并由解析器决定它是否属于已知异常。
     */
    @ExceptionHandler(RuntimeException.class)
    public R<Object> runtimeException(RuntimeException e, HttpServletResponse response) {
        return writeResolved(e, response);
    }

    /**
     * 处理非运行时异常；未知异常最终只返回安全保底消息。
     */
    @ExceptionHandler(Exception.class)
    public R<Object> handleException(Exception e, HttpServletResponse response) {
        return writeResolved(e, response);
    }

    /**
     * 统一写入异常响应，并根据解析结果区分业务告警和未知异常堆栈日志。
     *
     * @param exception 原始异常
     * @param response  当前 HTTP 响应
     * @return 与 HTTP 状态一致的统一失败响应
     */
    private R<Object> writeResolved(Throwable exception, HttpServletResponse response) {
        var resolution = exceptionResponseResolver.resolve(exception);
        response.setStatus(resolution.status().value());
        var requestContext = requestContext();
        if (resolution.logStackTrace()) {
            log.error("{}异常，method={}, uri={}, correlationId={}, status={}", LogPrefix.WEB.p(), requestContext.method(),
                    requestContext.uri(), correlationId(), resolution.status().value(), exception);
        } else {
            log.warn("{}请求失败，method={}, uri={}, correlationId={}, status={}, message={}", LogPrefix.WEB.p(),
                    requestContext.method(), requestContext.uri(), correlationId(), resolution.status().value(), resolution.message());
        }
        return R.failure(resolution.status(), resolution.message());
    }

    /** 获取当前请求的 method 和 URI；异步或非 Web 调用没有上下文时使用安全占位值。 */
    private static RequestContext requestContext() {
        var attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletAttributes) {
            HttpServletRequest request = servletAttributes.getRequest();
            return new RequestContext(request.getMethod(), request.getRequestURI());
        }
        return new RequestContext("unknown", "unknown");
    }

    private record RequestContext(String method, String uri) {
    }

    /** 获取当前请求关联标识，便于从日志定位一次完整请求。 */
    private static String correlationId() {
        String correlationId = RequestCorrelationContext.current().correlationId();
        return correlationId == null ? "unknown" : correlationId;
    }
}

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

import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.exception.BusinessRuleViolationException;
import com.devops00.spectra.common.exception.DataExistException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataScopeViolationException;
import com.devops00.spectra.common.exception.KaptchaExpiresException;
import com.devops00.spectra.common.exception.KaptchaNotMatchException;
import com.devops00.spectra.common.exception.NotImplementedException;
import com.devops00.spectra.common.exception.SchedulerDatabaseUnavailableException;
import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.common.exception.SecuritySecretUnavailableException;
import com.devops00.spectra.common.exception.SpectraException;
import com.devops00.spectra.common.security.policy.SecurityPolicyUnavailableException;
import com.devops00.spectra.framework.web.crypto.CryptoConfigurationException;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * 将 MVC 边界收到的异常统一解析为 HTTP 状态和安全响应消息。
 *
 * <p>解析器先识别具体的项目异常和技术边界异常，再处理已审核的标准应用异常，最后才使用固定的未知异常兜底。
 * 原始异常和 cause 只供上层日志使用，不会进入 {@link ExceptionResolution} 的消息。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/18
 */
@NullMarked
public class ExceptionResponseResolver {

    /** 未知异常对外统一使用的安全消息，避免把堆栈、SQL 或基础设施细节泄露给客户端。 */
    private static final String UNKNOWN_MESSAGE = "系统内部错误，请联系管理员";

    /**
     * 解析异常后的统一结果。
     *
     * @param status          对外 HTTP 状态
     * @param message         对外安全消息
     * @param logStackTrace   是否需要在边界层记录完整堆栈
     */
    public record ExceptionResolution(HttpStatus status, String message, boolean logStackTrace) {
    }

    /**
     * 解析异常及其 cause 链。
     *
     * @param exception 原始异常
     * @return 状态、消息和日志策略；永远不会返回 null
     */
    public ExceptionResolution resolve(Throwable exception) {
        // 按“业务语义优先、基础设施次之、未知异常兜底”的顺序匹配，避免父类异常抢先覆盖更具体的异常。
        var dataNotExist = findCause(exception, DataNotExistException.class);
        if (dataNotExist != null) {
            return known(HttpStatus.NOT_FOUND, messageOr(dataNotExist.getMessage(), "数据不存在"));
        }

        var dataExist = findCause(exception, DataExistException.class);
        if (dataExist != null) {
            return known(HttpStatus.CONFLICT, messageOr(dataExist.getMessage(), "数据已存在"));
        }

        var businessRule = findCause(exception, BusinessRuleViolationException.class);
        if (businessRule != null) {
            return known(HttpStatus.BAD_REQUEST, messageOr(businessRule.getMessage(), "业务规则校验失败"));
        }

        var dataScope = findCause(exception, DataScopeViolationException.class);
        if (dataScope != null) {
            return known(HttpStatus.FORBIDDEN, "数据范围不足");
        }

        var notImplemented = findCause(exception, NotImplementedException.class);
        if (notImplemented != null) {
            return known(HttpStatus.INTERNAL_SERVER_ERROR, "功能暂未实现");
        }

        var noResource = findCause(exception, NoResourceFoundException.class);
        if (noResource != null) {
            return known(HttpStatus.NOT_FOUND, "未找到资源");
        }

        var kaptchaNotMatch = findCause(exception, KaptchaNotMatchException.class);
        if (kaptchaNotMatch != null) {
            return known(HttpStatus.BAD_REQUEST, "验证码不匹配");
        }

        var kaptchaExpires = findCause(exception, KaptchaExpiresException.class);
        if (kaptchaExpires != null) {
            return known(HttpStatus.BAD_REQUEST, "验证码过期");
        }

        var scheduler = findCause(exception, SchedulerDatabaseUnavailableException.class);
        if (scheduler != null) {
            return infrastructure(HttpStatus.SERVICE_UNAVAILABLE, "调度服务暂不可用");
        }

        var redis = findCause(exception, SecurityRedisUnavailableException.class);
        if (redis != null) {
            return infrastructure(HttpStatus.SERVICE_UNAVAILABLE, "安全会话服务暂不可用");
        }

        var secret = findCause(exception, SecuritySecretUnavailableException.class);
        if (secret != null) {
            return infrastructure(HttpStatus.SERVICE_UNAVAILABLE, "安全密钥服务暂不可用");
        }

        var policy = findCause(exception, SecurityPolicyUnavailableException.class);
        if (policy != null) {
            return infrastructure(HttpStatus.SERVICE_UNAVAILABLE, "安全策略服务暂不可用");
        }

        var audit = findCause(exception, AuditService.AuditRecordingException.class);
        if (audit != null) {
            return infrastructure(HttpStatus.SERVICE_UNAVAILABLE, "审计服务暂不可用");
        }

        var cryptoConfiguration = findCause(exception, CryptoConfigurationException.class);
        if (cryptoConfiguration != null) {
            return infrastructure(HttpStatus.INTERNAL_SERVER_ERROR, "加密配置不可用");
        }

        var accessDenied = findCause(exception, AccessDeniedException.class);
        if (accessDenied != null) {
            return known(HttpStatus.FORBIDDEN, "权限不足");
        }

        var authentication = findCause(exception, AuthenticationException.class);
        if (authentication != null) {
            return resolveAuthentication(authentication);
        }

        var illegalArgument = findCause(exception, IllegalArgumentException.class);
        if (illegalArgument != null) {
            return known(HttpStatus.BAD_REQUEST, messageOr(illegalArgument.getMessage(), "请求参数无效"));
        }

        var illegalState = findCause(exception, IllegalStateException.class);
        if (illegalState != null) {
            return known(HttpStatus.CONFLICT, messageOr(illegalState.getMessage(), "当前状态不允许执行此操作"));
        }

        var projectException = findCause(exception, SpectraException.class);
        if (projectException != null) {
            return known(HttpStatus.INTERNAL_SERVER_ERROR, messageOr(projectException.getMessage(), "业务处理失败"));
        }

        // 只有没有任何已知异常语义时才进入这里；完整堆栈由 CommonExceptionAdvice 记录。
        return new ExceptionResolution(HttpStatus.INTERNAL_SERVER_ERROR, UNKNOWN_MESSAGE, true);
    }

    /**
     * 将 Spring Security 认证异常转换为稳定的登录态语义。
     *
     * <p>认证失败的原始消息可能包含框架或认证提供方细节，因此只有明确允许展示的密码错误消息会被保留。</p>
     *
     * @param exception Spring Security 认证异常
     * @return HTTP 401 及安全的认证失败消息
     */
    private static ExceptionResolution resolveAuthentication(AuthenticationException exception) {
        if (exception instanceof CredentialsExpiredException) {
            return known(HttpStatus.UNAUTHORIZED, "登录已过期");
        }
        if (exception instanceof InsufficientAuthenticationException) {
            return known(HttpStatus.UNAUTHORIZED, "用户未登录");
        }
        if (exception instanceof BadCredentialsException) {
            return known(HttpStatus.UNAUTHORIZED, messageOr(exception.getMessage(), "账号或密码错误"));
        }
        return known(HttpStatus.UNAUTHORIZED, "认证失败");
    }

    /** 已知业务异常：允许边界层记录摘要，不需要完整堆栈。 */
    private static ExceptionResolution known(HttpStatus status, String message) {
        return new ExceptionResolution(status, message, false);
    }

    /** 基础设施异常：对外返回安全消息，同时要求边界层保留完整堆栈用于排障。 */
    private static ExceptionResolution infrastructure(HttpStatus status, String message) {
        return new ExceptionResolution(status, message, true);
    }

    /** 统一处理异常消息为空或只包含空白的情况。 */
    private static String messageOr(String message, String fallback) {
        return message == null || message.isBlank() ? fallback : message;
    }

    /**
     * 在异常 cause 链中按类型查找异常。
     *
     * <p>使用对象身份集合而不是 {@code equals}，并防止异常链异常地形成环，避免错误处理再次阻塞请求线程。</p>
     *
     * @param source    原始异常
     * @param targetType 要查找的异常类型
     * @param <T>       异常类型
     * @return 找到的异常；未找到或输入为空时返回 null
     */
    private static <T extends Throwable> T findCause(Throwable source, Class<T> targetType) {
        if (source == null) {
            return null;
        }
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = source;
        while (current != null && visited.add(current)) {
            if (targetType.isInstance(current)) {
                return targetType.cast(current);
            }
            current = current.getCause();
        }
        return null;
    }
}

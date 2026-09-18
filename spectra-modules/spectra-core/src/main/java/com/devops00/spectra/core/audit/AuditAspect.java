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

package com.devops00.spectra.core.audit;

import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditContext;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.audit.RequestCorrelationContext;
import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.exception.SpectraException;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.framework.web.request.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link Audit} 统一技术入口。
 *
 * <p>成功、拒绝和返回失败状态的事件在业务事务内同步写入。异常事件等事务回滚完成后，
 * 通过独立事务写入统一审计表。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Slf4j
@Aspect
public class AuditAspect {

    private static final String CLIENT_TYPE_HEADER = "X-Client-Type";

    private static final int EVENT_TYPE_MAX_LENGTH = 100;

    private final SecurityContextAccessor securityContextAccessor;
    private final AuditService auditService;
    private final AuditSanitizer auditSanitizer;
    private final TransactionOperations transactionOperations;
    private final AuditFailureResolver failureResolver;
    private final AuditFailureRecorder failureRecorder;

    private final ExpressionParser parser = new SpelExpressionParser();

    public AuditAspect(SecurityContextAccessor securityContextAccessor,
                       AuditService auditService,
                       AuditSanitizer auditSanitizer,
                       TransactionOperations transactionOperations,
                       AuditFailureResolver failureResolver,
                       AuditFailureRecorder failureRecorder) {
        this.securityContextAccessor = securityContextAccessor;
        this.auditService = auditService;
        this.auditSanitizer = auditSanitizer;
        this.transactionOperations = transactionOperations;
        this.failureResolver = failureResolver;
        this.failureRecorder = failureRecorder;
    }

    /**
     * 收集调用元数据并在业务事务边界内提交审计事件。
     *
     * @param point 当前方法调用
     * @return 业务方法返回值
     * @throws Throwable 业务方法原始异常
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    @Around("@annotation(com.devops00.spectra.common.audit.Audit)")
    public Object handleAround(ProceedingJoinPoint point) throws Throwable {
        Method method = resolveMethod(point);
        AuditDescriptor descriptor = resolveDescriptor(method, point);
        long startedAt = System.nanoTime();
        var current = resolveCorrelationContext();
        try {
            return RequestCorrelationContext.callWithMdc(current, () -> transactionOperations.execute(status -> {
                Object result;
                try {
                    result = point.proceed();
                } catch (Throwable failure) {
                    throw new AuditedInvocationException(failure);
                }
                if (descriptor != null) {
                    submit(point, method, descriptor, result, null, startedAt);
                }
                return result;
            }));
        } catch (AuditedInvocationException exception) {
            Throwable original = exception.original();
            if (descriptor != null) {
                recordFailureAfterRollback(point, method, descriptor, original, startedAt);
            }
            throw original;
        }
    }

    /**
     * 记录失败。
     */
    private void recordFailureAfterRollback(ProceedingJoinPoint point,
                                            Method method,
                                            AuditDescriptor descriptor,
                                            Throwable failure,
                                            long startedAt) {
        AuditRecord record = createRecord(point, method, descriptor, null, failure, startedAt);
        if (TransactionSynchronizationManager.isSynchronizationActive()
                && TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    recordFailure(record, failure, descriptor);
                }
            });
            return;
        }
        recordFailure(record, failure, descriptor);
    }

    /**
     * 记录失败。
     */
    private void recordFailure(AuditRecord record, Throwable failure, AuditDescriptor descriptor) {
        try {
            failureRecorder.record(record);
        } catch (RuntimeException recordingFailure) {
            if (recordingFailure != failure) {
                failure.addSuppressed(recordingFailure);
            }
            log.error("{}失败审计写入失败: category={}, eventType={}, cause={}",
                    LogPrefix.LOG.p(), descriptor.category(), descriptor.eventType(),
                    recordingFailure.getClass().getSimpleName(), recordingFailure);
        }
    }

    /**
     * 解析方法。
     */
    private Method resolveMethod(ProceedingJoinPoint point) {
        if (!(point.getSignature() instanceof MethodSignature signature)) {
            return null;
        }
        return signature.getMethod();
    }

    /**
     * 解析审计。
     */
    private AuditDescriptor resolveDescriptor(Method method, ProceedingJoinPoint point) {
        if (method == null) {
            return null;
        }
        Audit audit = method.getAnnotation(Audit.class);
        if (audit == null) {
            return null;
        }
        String eventType = audit.eventType().isBlank() ? generatedEventType(method) : audit.eventType();
        return new AuditDescriptor(audit.category(), eventType, parseDescription(audit.value(), method, point),
                audit.captureArguments(), audit.captureResult());
    }

    /**
     * 生成稳定事件类型，并确保其长度符合统一表字段限制。
     */
    private static String generatedEventType(Method method) {
        String qualifiedName = method.getDeclaringClass().getName() + "#" + method.getName();
        return qualifiedName.length() <= EVENT_TYPE_MAX_LENGTH
                ? qualifiedName
                : method.getDeclaringClass().getSimpleName() + "#" + method.getName();
    }

    /**
     * 解析说明。
     */
    private String parseDescription(String expression, Method method, ProceedingJoinPoint point) {
        if (expression == null || expression.isBlank()) {
            return null;
        }
        boolean quotedLiteral = expression.length() > 1
                && expression.charAt(0) == '\''
                && expression.charAt(expression.length() - 1) == '\'';
        if (!expression.contains("#") && !quotedLiteral) {
            return expression;
        }
        if (expression.contains("T(")) {
            log.warn("{}拒绝包含类型引用的审计描述表达式: {}", LogPrefix.LOG.p(), method.getName());
            return expression;
        }
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            MethodSignature signature = (MethodSignature) point.getSignature();
            String[] parameterNames = signature.getParameterNames();
            Object[] args = point.getArgs();
            for (int index = 0; index < parameterNames.length && index < args.length; index++) {
                context.setVariable(parameterNames[index], args[index]);
            }
            return parser.parseExpression(expression).getValue(context, String.class);
        } catch (RuntimeException exception) {
            log.warn("{}审计描述表达式解析失败: method={}, error={}",
                    LogPrefix.LOG.p(), method.getName(), exception.getClass().getSimpleName());
            return expression;
        }
    }

    /**
     * 处理审计相关数据。
     */
    private void submit(ProceedingJoinPoint point,
                        Method method,
                        AuditDescriptor descriptor,
                        Object result,
                        Throwable failure,
                        long startedAt) {
        AuditRecord record = createRecord(point, method, descriptor, result, failure, startedAt);
        try {
            auditService.record(record);
        } catch (AuditService.AuditRecordingException exception) {
            log.error("{}审计记录提交失败，业务事务将回滚: category={}, eventType={}",
                    LogPrefix.LOG.p(), descriptor.category(), descriptor.eventType(), exception);
            throw exception;
        }
    }

    /**
     * 构建记录。
     */
    private AuditRecord createRecord(ProceedingJoinPoint point,
                                     Method method,
                                     AuditDescriptor descriptor,
                                     Object result,
                                     Throwable failure,
                                     long startedAt) {
        RequestMetadata request = requestMetadata();
        Map<String, Object> before = new LinkedHashMap<>();
        if (descriptor.captureArguments()) {
            before.put("arguments", extractArguments(point));
        }
        if (request.webRequest()) {
            before.put("request", Map.of("method", request.method(), "url", request.url()));
        }

        Map<String, Object> after = new LinkedHashMap<>();
        if (failure == null && descriptor.captureResult()) {
            after.put("result", result);
        }
        if (request.status() != null) {
            after.put("status", request.status());
        }
        long durationMs = Math.max(0L, (System.nanoTime() - startedAt) / 1_000_000L);
        after.put("durationMs", durationMs);

        AuditContext context = new AuditContext(
                securityContextAccessor.currentUserId(),
                request.requestId(),
                request.correlationId(),
                request.client(),
                request.ip(),
                request.userAgent());
        AuditRecord.Result recordResult = failure == null
                ? resultOf(request.status())
                : AuditRecord.Result.FAILED;
        AuditRecord.Failure failureDetails = failure == null ? null : failureResolver.resolve(failure);
        return new AuditRecord(
                null,
                descriptor.category(),
                descriptor.eventType(),
                null,
                recordResult,
                null,
                context,
                auditSanitizer.sanitize(before),
                auditSanitizer.sanitize(after),
                descriptor.reason(),
                new AuditRecord.HttpSummary(request.method(), request.url(), request.status(), durationMs),
                failureDetails);
    }

    /**
     * 处理结果相关数据。
     */
    private static AuditRecord.Result resultOf(Integer status) {
        if (status != null
                && (status == HttpServletResponse.SC_UNAUTHORIZED
                        || status == HttpServletResponse.SC_FORBIDDEN)) {
            return AuditRecord.Result.DENIED;
        }
        if (status != null && status >= HttpServletResponse.SC_BAD_REQUEST) {
            return AuditRecord.Result.FAILED;
        }
        return AuditRecord.Result.SUCCEEDED;
    }

    /**
     * 处理请求元数据相关数据。
     */
    private RequestMetadata requestMetadata() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            var trace = RequestCorrelationContext.current();
            return RequestMetadata.empty(trace.requestId(), trace.correlationId());
        }
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        var trace = RequestCorrelationContext.current();
        Integer status = response == null ? null : response.getStatus();
        return new RequestMetadata(
                true,
                request.getMethod(),
                request.getRequestURI(),
                trace.requestId(),
                trace.correlationId(),
                header(request, CLIENT_TYPE_HEADER),
                IpUtils.getClientIP(request),
                header(request, "User-Agent"),
                status);
    }

    /**
     * 解析关联上下文。
     */
    private RequestCorrelationContext.Context resolveCorrelationContext() {
        var current = RequestCorrelationContext.current();
        if (!current.isEmpty()) {
            return current;
        }
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            var request = attributes.getRequest();
            return RequestCorrelationContext.forHttp(
                    request.getHeader(RequestCorrelationContext.REQUEST_ID_HEADER),
                    request.getHeader(RequestCorrelationContext.CORRELATION_ID_HEADER));
        }
        return RequestCorrelationContext.forTask(null);
    }

    /**
     * 处理审计相关数据。
     */
    private static String header(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return value == null || value.isBlank() ? null : value;
    }

    /**
     * 处理审计相关数据。
     */
    private List<Object> extractArguments(ProceedingJoinPoint point) {
        return Arrays.stream(point.getArgs())
                .filter(argument -> argument != null)
                .filter(argument -> !(argument instanceof MultipartFile))
                .filter(argument -> !(argument instanceof HttpServletRequest))
                .filter(argument -> !(argument instanceof HttpServletResponse))
                .filter(argument -> !(argument instanceof BindingResult))
                .toList();
    }

    /**
     * 承载审计相关的不可变数据。
     *
     * @param category         业务类别
     * @param eventType        事件类型
     * @param reason           本次操作或审计事件对应的原因
     * @param captureArguments 是否在审计事件中记录方法参数
     * @param captureResult    是否记录被审计方法的返回值
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    private record AuditDescriptor(AuditCategory category,
                                   String eventType,
                                   String reason,
                                   boolean captureArguments,
                                   boolean captureResult) {
    }

    /**
     * 承载请求元数据相关的不可变数据。
     *
     * @param webRequest    Web请求
     * @param method        HTTP 请求方法
     * @param url           HTTP 请求地址
     * @param requestId     请求标识
     * @param correlationId 关联标识
     * @param client        发起请求的客户端信息
     * @param ip            发起请求的客户端 IP 地址
     * @param userAgent     发起请求的客户端 User-Agent
     * @param status        业务状态
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    private record RequestMetadata(boolean webRequest,
                                   String method,
                                   String url,
                                   String requestId,
                                   String correlationId,
                                   String client,
                                   String ip,
                                   String userAgent,
                                   Integer status) {

        /**
         * 判断请求元数据。
         */
        private static RequestMetadata empty(String requestId, String correlationId) {
            return new RequestMetadata(false, null, null, requestId, correlationId,
                    null, null, null, null);
        }
    }

    /**
     * 表示异常处理过程中发生的异常。
     *
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    private static final class AuditedInvocationException extends SpectraException {

        private final Throwable original;

        private AuditedInvocationException(Throwable original) {
            super(original);
            this.original = original;
        }

        /**
         * 处理异常相关数据。
         */
        private Throwable original() {
            return original;
        }
    }
}

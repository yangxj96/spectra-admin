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
import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.utils.IpUtils;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
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
 * <p>切面只收集调用元数据并同步提交 {@link AuditRecord}，不依赖具体日志表或 Mapper。
 * 普通操作记录由 Core 的操作日志 sink 写入 PostgreSQL outbox；安全记录由安全审计 sink
 * 同步写入事实表。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
@Slf4j
@Aspect
public class AuditAspect {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CLIENT_TYPE_HEADER = "X-Client-Type";

    private final SecurityContextAccessor securityContextAccessor;

    private final AuditService auditService;

    private final AuditSanitizer auditSanitizer;

    private final TransactionOperations transactionOperations;

    private final ExpressionParser parser = new SpelExpressionParser();

    public AuditAspect(SecurityContextAccessor securityContextAccessor,
                       AuditService auditService,
                       AuditSanitizer auditSanitizer,
                       TransactionOperations transactionOperations) {
        this.securityContextAccessor = securityContextAccessor;
        this.auditService = auditService;
        this.auditSanitizer = auditSanitizer;
        this.transactionOperations = transactionOperations;
    }

    /**
     * 收集调用元数据并提交统一审计事件。
     *
     * @param point 当前方法调用
     * @return 业务方法返回值
     * @throws Throwable 业务方法原始异常
     */
    @Around("@annotation(com.devops00.spectra.common.audit.Audit)")
    public Object handleAround(ProceedingJoinPoint point) throws Throwable {
        Method method = resolveMethod(point);
        AuditDescriptor descriptor = resolveDescriptor(method, point);
        long startedAt = System.nanoTime();
        try {
            return transactionOperations.execute(status -> {
                Object result = null;
                Throwable failure = null;
                try {
                    result = point.proceed();
                    return result;
                } catch (Throwable ex) {
                    failure = ex;
                    throw new AuditedInvocationException(ex);
                } finally {
                    if (descriptor != null) {
                        try {
                            submit(point, method, descriptor, result, failure, startedAt);
                        } catch (AuditService.AuditRecordingException auditFailure) {
                            if (failure != null) {
                                failure.addSuppressed(auditFailure);
                            } else {
                                throw auditFailure;
                            }
                        }
                    }
                }
            });
        } catch (AuditedInvocationException ex) {
            throw ex.original;
        }
    }

    private Method resolveMethod(ProceedingJoinPoint point) {
        if (!(point.getSignature() instanceof MethodSignature signature)) {
            return null;
        }
        return signature.getMethod();
    }

    private AuditDescriptor resolveDescriptor(Method method, ProceedingJoinPoint point) {
        if (method == null) {
            return null;
        }

        Audit audit = method.getAnnotation(Audit.class);
        if (audit != null) {
            String eventType = audit.eventType().isBlank()
                    ? method.getDeclaringClass().getName() + "#" + method.getName()
                    : audit.eventType();
            return new AuditDescriptor(audit.category(), eventType, parseDescription(audit.value(), method, point));
        }

        return null;
    }

    private String parseDescription(String expression, Method method, ProceedingJoinPoint point) {
        if (expression == null || expression.isBlank()) {
            return null;
        }
        if (!expression.contains("#")) {
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
        } catch (RuntimeException ex) {
            // 描述解析失败不应改变业务结果，且不打印参数值，避免将敏感数据带入技术日志。
            log.warn("{}审计描述表达式解析失败: method={}, error={}",
                    LogPrefix.LOG.p(), method.getName(), ex.getClass().getSimpleName());
            return expression;
        }
    }

    private void submit(ProceedingJoinPoint point,
                        Method method,
                        AuditDescriptor descriptor,
                        Object result,
                        Throwable failure,
                        long startedAt) {
        RequestMetadata request = requestMetadata();
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("arguments", extractArguments(point));
        if (request.webRequest()) {
            before.put("request", Map.of("method", request.method(), "url", request.url()));
        }

        Map<String, Object> after = new LinkedHashMap<>();
        after.put("result", result);
        after.put("status", request.status());
        after.put("durationMs", (System.nanoTime() - startedAt) / 1_000_000L);
        if (failure != null) {
            after.put("errorType", failure.getClass().getName());
        }

        Map<String, Object> sanitizedBefore = auditSanitizer.sanitize(before);
        Map<String, Object> sanitizedAfter = auditSanitizer.sanitize(after);
        AuditContext context = new AuditContext(
                securityContextAccessor.currentUserId(),
                request.requestId(),
                request.correlationId(),
                request.client(),
                request.ip(),
                request.userAgent());
        AuditRecord record = new AuditRecord(
                null,
                descriptor.category(),
                descriptor.eventType(),
                null,
                resultOf(request.status(), failure),
                null,
                context,
                sanitizedBefore,
                sanitizedAfter,
                descriptor.reason());

        try {
            auditService.record(record);
        } catch (AuditService.AuditRecordingException ex) {
            log.error("{}审计记录提交失败，业务事务将回滚: category={}, eventType={}",
                    LogPrefix.LOG.p(), descriptor.category(), descriptor.eventType(), ex);
            throw ex;
        }
    }

    private AuditRecord.Result resultOf(short status, Throwable failure) {
        if (status == HttpServletResponse.SC_UNAUTHORIZED || status == HttpServletResponse.SC_FORBIDDEN) {
            return AuditRecord.Result.DENIED;
        }
        if (failure != null || status >= HttpServletResponse.SC_BAD_REQUEST) {
            return AuditRecord.Result.FAILED;
        }
        return AuditRecord.Result.SUCCEEDED;
    }

    private RequestMetadata requestMetadata() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return RequestMetadata.empty();
        }
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        return new RequestMetadata(
                true,
                request.getMethod(),
                request.getRequestURI(),
                header(request, REQUEST_ID_HEADER),
                header(request, CORRELATION_ID_HEADER),
                header(request, CLIENT_TYPE_HEADER),
                IpUtils.getClientIP(request),
                header(request, "User-Agent"),
                response == null ? 0 : (short) response.getStatus());
    }

    private String header(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return value == null || value.isBlank() ? null : value;
    }

    private List<Object> extractArguments(ProceedingJoinPoint point) {
        return Arrays.stream(point.getArgs())
                .filter(argument -> argument != null)
                .filter(argument -> !(argument instanceof MultipartFile))
                .filter(argument -> !(argument instanceof HttpServletRequest))
                .filter(argument -> !(argument instanceof HttpServletResponse))
                .filter(argument -> !(argument instanceof BindingResult))
                .toList();
    }

    private record AuditDescriptor(AuditCategory category, String eventType, String reason) {
    }

    private record RequestMetadata(boolean webRequest,
                                   String method,
                                   String url,
                                   String requestId,
                                   String correlationId,
                                   String client,
                                   String ip,
                                   String userAgent,
                                   short status) {

        private static RequestMetadata empty() {
            return new RequestMetadata(false, null, null, null, null, null, null, null, (short) 0);
        }
    }

    private static final class AuditedInvocationException extends RuntimeException {

        private final Throwable original;

        private AuditedInvocationException(Throwable original) {
            super(original);
            this.original = original;
        }
    }
}

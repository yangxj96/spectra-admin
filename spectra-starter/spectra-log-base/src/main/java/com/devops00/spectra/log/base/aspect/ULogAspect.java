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

package com.devops00.spectra.log.base.aspect;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.utils.IpUtils;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.log.base.entity.ULogEntity;
import com.devops00.spectra.log.base.enums.SysLogType;
import com.devops00.spectra.log.base.publisher.ULogEventPublisher;
import com.devops00.spectra.security.base.holder.SecUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jspecify.annotations.Nullable;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

/**
 * ULog注解AOP环绕切面
 *
 * 彻底移除 ThreadLocal，完美防范内存泄漏、参数暴雷及长文本溢出
 *
 * @author yangxj96
 * @version 1.1
 * @since 2025/7/23 15:44
 */
@Slf4j
@Aspect
public class ULogAspect {

    private final ExpressionParser parser = new SpelExpressionParser();

    @Resource
    private ULogEventPublisher publisher;

    /**
     * 改用 @Around 环绕通知
     *
     * 掌控整个执行链路，天然线程隔离，彻底绝育内存泄漏风险
     *
     * @param point
     *            入点
     * @param annotation
     *            注解
     */
    @Around("@annotation(annotation)")
    public Object handleAround(ProceedingJoinPoint point, ULog annotation) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;

        // 定义一个变量，用来装方法执行前解析出来的日志说明
        String preParsedExplain = null;

        try {
            // 在目标方法执行前，趁着登录上下文还在，提前解析 SpEL 静态方法
            if (!annotation.value().isEmpty()) {
                preParsedExplain = parseSpel(annotation.value(), point);
                // 此时 preParsedExplain 已经稳稳拿到了 "用户[019bdfa5-...]登出系统" 文本
            }

            // 执行真正的目标 Controller 业务方法
            result = point.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            // 原封不动抛出，确保不破坏原有的全局异常拦截器（@RestControllerAdvice）
            throw e;
        } finally {
            long timeCost = System.currentTimeMillis() - startTime;
            // 无论成功还是失败，100% 触发日志收尾落库
            executeLogSafely(point, annotation, exception, result, timeCost, preParsedExplain);
        }
    }

    private void executeLogSafely(ProceedingJoinPoint point, ULog annotation, @Nullable Exception e, @Nullable Object result, long timeCost,
            String preParsedExplain) {
        try {
            var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                log.debug(LogPrefix.LOG.f("非 Web 请求上下文，跳过日志记录"));
                return;
            }

            HttpServletRequest request = attributes.getRequest();
            HttpServletResponse response = attributes.getResponse();

            var datum = new ULogEntity();

            // 1. 智能识别日志类型：若有异常，强制升级为 SYSTEM_ERROR
            if (e != null) {
                datum.setType(SysLogType.SYSTEM_ERROR);
                // 仅截取错误核心摘要，防止过长的 Exception Stack 撑爆数据库字段
                String errorMsg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
                datum.setExplain("[异常捕获] " + annotation.value() + " -> 原因: " + errorMsg);
            } else {
                // 2. 正常情况下，支持 SpEL 动态解析
                datum.setType(annotation.type());
                datum.setExplain(preParsedExplain);
            }

            // 3. 安全提取并清洗请求入参
            datum.setArgs(extractAndCleanArgs(point));

            // 4. 安全记录响应出参（进行最大长度裁剪防护，防止大 List 撑爆内存）
            datum.setResult(cleanResult(result));

            // 5. 补充基础物理属性
            datum.setIp(IpUtils.getClientIP(request));
            datum.setMethod(request.getMethod());
            datum.setUrl(request.getRequestURI());
            datum.setStatus(getHttpResponseStatus(response));
            datum.setTimeCost(timeCost);

            // 6. 异步发布事件
            datum.setCurrentId(SecUtil.getCurrentUserId());
            publisher.save(datum);
        } catch (Exception ex) {
            // AOP 本身绝对不能向外抛出任何异常，绝不能因为记录日志失败而导致原本成功的业务回滚
            log.error("ULog 切面内部发生防御性崩溃: {}", ex.getMessage(), ex);
        }
    }

    /**
     * 轻量化提纯：只清洗，不序列化，不伤主线程性能
     */
    private Object[] extractAndCleanArgs(ProceedingJoinPoint point) {
        Object[] args = point.getArgs();
        if (args == null || args.length == 0) {
            return new Object[0];
        }
        // 依旧干净地过滤掉无法打包的 Web 核心基础对象
        return Arrays.stream(args)
                .filter(arg -> arg != null
                    && !(arg instanceof MultipartFile
                        || arg instanceof HttpServletRequest
                        || arg instanceof HttpServletResponse
                        || arg instanceof org.springframework.validation.BindingResult))
                .toArray();
    }

    /**
     * 防御性提纯响应返回值，防止大对象溢出
     */
    private @Nullable Object cleanResult(@Nullable Object result) {
        return result;
    }

    /**
     * 解析 SpEL 表达式，让 ULog 的说明内容支持动态传参
     */
    private String parseSpel(String spelExpression, ProceedingJoinPoint point) {
        if (spelExpression == null || spelExpression.isBlank()) {
            return "";
        }

        // 💡 性能轻量优化：如果既没有冒号（单引号）、也没有井号、也没有 T(，说明它 100% 是个普通纯文本
        if (!spelExpression.contains("#") && !spelExpression.contains("T(") && !spelExpression.contains("'")) {
            return spelExpression;
        }

        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            Object[] args = point.getArgs();

            // 使用 StandardEvaluationContext，完美承载 T() 静态方法与高级特性
            EvaluationContext context = new StandardEvaluationContext();

            // 绑定参数名，支持 #params.username 动态解析
            String[] parameterNames = signature.getParameterNames();
            if (parameterNames != null) {
                for (int i = 0; i < parameterNames.length; i++) {
                    context.setVariable(parameterNames[i], args[i]);
                }
            }

            // 去掉 templateParserContext！直接单刀直入解析纯 SpEL 表达式
            return parser.parseExpression(spelExpression).getValue(context, String.class);
        } catch (Exception e) {
            log.warn("SpEL 表达式解析失败, 表达式: {}, 错误: {}", spelExpression, e.getMessage());
            return spelExpression; // 解析失败则兜底返回原始表达式文本
        }
    }

    private short getHttpResponseStatus(@Nullable HttpServletResponse response) {
        if (response == null) {
            return 500;
        }
        return (short) response.getStatus();
    }
}

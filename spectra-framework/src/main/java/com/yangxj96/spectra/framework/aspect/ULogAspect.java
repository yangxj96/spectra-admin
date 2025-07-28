/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.framework.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangxj96.spectra.common.annotation.ULog;
import com.yangxj96.spectra.common.javabean.ULogEntity;
import com.yangxj96.spectra.common.utils.IpUtils;
import com.yangxj96.spectra.framework.publisher.ULogEventPublisher;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.NamedThreadLocal;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * ULog注解AOP切面
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
@Aspect
public class ULogAspect {

    private static final String PREFIX = "[操作日志切面]:";

    @Resource
    private ULogEventPublisher publisher;

    @Resource
    private ObjectMapper om;

    /**
     * 计算操作消耗时间
     */
    private static final ThreadLocal<Long> TIME_THREADLOCAL = new NamedThreadLocal<>("Cost Time");

    // 请求前
    @Before("@annotation(annotation)")
    public void handleBefore(JoinPoint point, ULog annotation) {
        TIME_THREADLOCAL.set(System.currentTimeMillis());
    }

    // 请求后
    @AfterReturning(value = "@annotation(annotation)", returning = "result")
    public void handleAfter(JoinPoint point, ULog annotation, Object result) {
        handleLog(point, annotation, null, result);
    }

    // 请求发生错误
    @AfterThrowing(value = "@annotation(annotation)", throwing = "e")
    public void handleThrowing(JoinPoint point, ULog annotation, Exception e) {
        handleLog(point, annotation, e, null);
    }

    /**
     * 处理日志记录
     *
     * @param point      切入点
     * @param annotation 注解
     * @param e          错误信息
     * @param jsonResult 响应信息
     */
    protected void handleLog(final JoinPoint point, ULog annotation, final Exception e, Object jsonResult) {
        log.atDebug().log(PREFIX + "操作日志-开始记录");
        try {
            // 获取当前用户
            // 获取请求上下文
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            HttpServletResponse response = attributes.getResponse();

            // 初始化记录实体
            ULogEntity datum = ULogEntity
                    .builder()
                    .explain(annotation.value())
                    .ip(IpUtils.getClientIP(request))
                    .method(request.getMethod())
                    .url(request.getRequestURI())
                    .status((short) response.getStatus())
                    .result(safeWriteValueAsString(jsonResult))
                    .timeCost(System.currentTimeMillis() - TIME_THREADLOCAL.get())
                    .token(StpUtil.getTokenValue())
                    .build();
            publisher.save(datum);
            log.atDebug().log(PREFIX + "操作日志-记录结束");
        } catch (Exception ex) {
            log.atError().log("记录日志异常:{}", e.getMessage(), ex);
        } finally {
            TIME_THREADLOCAL.remove();
        }
        log.atDebug().log(PREFIX + "操作日志-记录结束");
    }


    /**
     * 安全序列化
     *
     * @param obj 对象
     * @return null或者字符串
     */
    private String safeWriteValueAsString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return om.writeValueAsString(obj);
        } catch (Exception e) {
            log.atError().log(PREFIX + "JSON 序列化失败: {}", obj.getClass());
            return null;
        }
    }

}

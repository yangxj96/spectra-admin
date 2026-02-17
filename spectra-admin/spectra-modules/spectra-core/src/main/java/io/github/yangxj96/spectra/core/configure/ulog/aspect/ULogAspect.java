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

package io.github.yangxj96.spectra.core.configure.ulog.aspect;


import io.github.yangxj96.spectra.common.utils.IpUtils;
import io.github.yangxj96.spectra.core.configure.ulog.annotation.ULog;
import io.github.yangxj96.spectra.core.configure.ulog.entity.ULogEntity;
import io.github.yangxj96.spectra.core.configure.ulog.publisher.ULogEventPublisher;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.jspecify.annotations.Nullable;
import org.springframework.core.NamedThreadLocal;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.ObjectMapper;

/// ULog注解AOP切面
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-6-14
@Slf4j
@Aspect
public class ULogAspect {

    private static final String PREFIX = "[ULogAspect]:";

    @Resource
    private ULogEventPublisher publisher;

    @Resource
    private ObjectMapper om;

    /// 计算操作消耗时间
    private static final ThreadLocal<Long> TIME_THREADLOCAL = new NamedThreadLocal<>("Cost Time");

    /// 请求前
    @Before("@annotation(annotation)")
    @SuppressWarnings("unused")
    public void handleBefore(JoinPoint point, ULog annotation) {
        TIME_THREADLOCAL.set(System.currentTimeMillis());
    }

    /// 请求后
    @AfterReturning(value = "@annotation(annotation)", returning = "result")
    public void handleAfter(JoinPoint point, ULog annotation, Object result) {
        handleLog(point, annotation, null, result);
    }

    /// 请求发生错误
    @AfterThrowing(value = "@annotation(annotation)", throwing = "e")
    public void handleThrowing(JoinPoint point, ULog annotation, Exception e) {
        handleLog(point, annotation, e, null);
    }

    /// 处理日志记录
    ///
    /// @param point      切入点
    /// @param annotation 注解
    /// @param e          错误信息
    /// @param jsonResult 响应信息
    @SuppressWarnings("unused")
    protected void handleLog(final JoinPoint point, ULog annotation, @Nullable final Exception e, @Nullable Object jsonResult) {
        try {
            // 获取请求上下文
            var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                log.warn(PREFIX + "非 Web 请求上下文，跳过日志记录");
                return;
            }

            var request = attributes.getRequest();
            var response = attributes.getResponse();

            log.debug("{}操作日志-开始记录,请求方法:{}", PREFIX, request.getMethod());

            // 初始化记录实体
            var datum = new ULogEntity();
            datum.setType(annotation.type());
            datum.setExplain(annotation.value());
            datum.setIp(IpUtils.getClientIP(request));
            datum.setMethod(request.getMethod());
            datum.setUrl(request.getRequestURI());
            datum.setStatus(getHttpResponseStatus(response));
            datum.setResult(safeWriteValueAsString(jsonResult));
            datum.setTimeCost(System.currentTimeMillis() - TIME_THREADLOCAL.get());
            // TODO 尝试获取当前用户,不要让mybatis plus去获取,因为要用异步处理,获取不到上下文
            //datum.setCurrentId(SecUtil.getCurrentUserId());
            publisher.save(datum);
            log.debug(PREFIX + "操作日志-记录结束");
        } catch (Exception ex) {
            log.error("记录日志异常:{}", ex.getMessage(), ex);
        } finally {
            TIME_THREADLOCAL.remove();
        }
        log.debug(PREFIX + "操作日志-记录结束");
    }


    /// 安全序列化
    ///
    /// @param obj 对象
    /// @return null或者字符串
    private @Nullable String safeWriteValueAsString(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return om.writeValueAsString(obj);
        } catch (Exception e) {
            log.error(PREFIX + "JSON 序列化失败: {}", obj.getClass(), e);
            return null;
        }
    }

    /// 获取状态码,如果失败则返回未知
    ///
    /// @param response Http响应
    /// @return 状态码
    private short getHttpResponseStatus(@Nullable HttpServletResponse response) {
        short status = 500;
        if (response == null) {
            return status;
        }
        try {
            status = (short) response.getStatus();
        } catch (IllegalStateException e) {
            log.error("无法获取响应状态码", e);
        }
        return status;
    }

}

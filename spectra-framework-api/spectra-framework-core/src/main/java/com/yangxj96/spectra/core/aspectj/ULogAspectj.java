package com.yangxj96.spectra.core.aspectj;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangxj96.spectra.core.annotation.ULog;
import com.yangxj96.spectra.core.entity.dto.OperationLog;
import com.yangxj96.spectra.core.service.OperationLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * ULog注解AOP切面
 *
 * @author 杨新杰
 * @since 2025/6/5 10:46
 */
@Slf4j
@Aspect
@Component
public class ULogAspectj {

    @Resource
    private OperationLogService operationLogService;

    @Resource
    private ObjectMapper om;

    /**
     * 对使用了ULog注解的方法进行切面拦截处理
     *
     * @param joinPoint  入口点
     * @param annotation 注解
     * @return obj
     */
    @Around("@annotation(annotation)")
    public Object handle(ProceedingJoinPoint joinPoint, ULog annotation) throws Throwable {
        log.atDebug().log("操作日志-开始记录");
        // 获取注解中的树形
        long startTime = System.currentTimeMillis();
        // 获取请求上下文
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 初始化记录实体
        OperationLog datum = new OperationLog();
        datum.setCreatedBy(StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null);
        datum.setUpdatedBy(StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null);
        datum.setExplain(annotation.value());
        datum.setArgs(safeWriteValueAsString(getArgs(joinPoint)));
        datum.setIp(getClientIP(request));
        datum.setMethod(request.getMethod());
        datum.setUrl(request.getRequestURI());

        Object result = null;
        try {
            // 执行程序
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            log.atError().log("操作日志-AOP切面异常", e);
            throw e;
        } finally {
            HttpServletResponse response = attributes.getResponse();

            // 也有可能在执行后才能获取到UID,所以执行完成后在尝试获取用户ID
            if (datum.getCreatedBy() == null || datum.getUpdatedBy() == null) {
                datum.setCreatedBy(StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null);
                datum.setUpdatedBy(StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null);
            }
            datum.setStatus((short) response.getStatus());
            datum.setResult(safeWriteValueAsString(result));
            datum.setTimeCost(System.currentTimeMillis() - startTime);
            operationLogService.save(datum);

            log.atDebug().log("操作日志-记录结束");
        }
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
            log.atError().log("JSON 序列化失败: {}", obj.getClass());
            return null;
        }
    }

    /**
     * 获取请求参数（适用于 Controller 方法参数为简单对象或基本类型）
     */
    private Object getArgs(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < parameterNames.length; i++) {
            params.put(parameterNames[i], args[i]);
        }

        return params;
    }

    /**
     * 获取客户端真实 IP（考虑代理）
     */
    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}

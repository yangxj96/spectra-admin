package com.devops00.spectra.ai.starter.configuration;


import com.devops00.spectra.ai.starter.middleware.LoggerMiddleware;
import com.devops00.spectra.ai.starter.middleware.SecurityMiddleware;
import com.devops00.spectra.common.constant.LogPrefix;
import io.agentscope.core.middleware.MiddlewareBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * 中间件配置器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/6/8 14:10
 */
@Slf4j
public class MiddlewareConfiguration {

    /// 安全中间件
    @Bean
    @ConditionalOnClass(name = "com.devops00.spectra.security.starter.autoconfiguration.SecurityAutoConfiguration")
    public MiddlewareBase securityMiddleware() {
        log.debug("{}加载安全中间件", LogPrefix.AI.p());
        return new SecurityMiddleware();
    }


    /// 日志中间件
    @Bean
    public LoggerMiddleware loggerMiddleware() {
        log.debug("{}加载日志中间件", LogPrefix.AI.p());
        return new LoggerMiddleware();
    }

}


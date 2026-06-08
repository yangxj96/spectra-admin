package com.devops00.spectra.ai.starter.configuration;


import com.devops00.spectra.ai.starter.middleware.LoggerMiddleware;
import com.devops00.spectra.ai.starter.middleware.SecurityMiddleware;
import io.agentscope.core.middleware.MiddlewareBase;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 中间件配置器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/6/8 14:10
 */
@Configuration
public class MiddlewareConfiguration {

    /// 安全中间件
    @Bean
    @ConditionalOnClass(name = "com.devops00.spectra.security.starter.autoconfiguration.SecurityAutoConfiguration")
    public MiddlewareBase securityMiddleware() {
        return new SecurityMiddleware();
    }


    /// 日志中间件
    @Bean
    public LoggerMiddleware loggerMiddleware() {
        return new LoggerMiddleware();
    }

}


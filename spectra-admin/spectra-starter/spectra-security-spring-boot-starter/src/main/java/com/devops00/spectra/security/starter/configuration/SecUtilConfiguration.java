package com.devops00.spectra.security.starter.configuration;


import com.devops00.spectra.security.base.holder.SecHolderStrategy;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.starter.holder.SecStrategyBridge;
import com.devops00.spectra.security.starter.strategy.RedisSecHolderStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import tools.jackson.databind.ObjectMapper;

/// SecuUtil工具相关配置
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/2/19 22:37
@Slf4j
public class SecUtilConfiguration {


    /// SecUtil 工具桥接
    ///
    /// @param strategy 具体策略
    @Bean
    public SecStrategyBridge secStrategyBridge(SecHolderStrategy strategy) {
        return new SecStrategyBridge(strategy);
    }

    /// 使用Redis操作SecUtil具体业务的策略实现
    ///
    /// @param om               Security使用的ObjectMapper
    /// @param redis            Security使用的RedisTemplate
    /// @param properties       安全配置
    /// @param tokenTtlStrategy token的ttl策略
    @Bean(name = "sec")
    @ConditionalOnProperty(prefix = "spectra.security", name = "sec-mode", havingValue = "REDIS", matchIfMissing = true)
    public RedisSecHolderStrategy redisSecHolderStrategy(
            @Qualifier("securityObjectMapper") ObjectMapper om,
            @Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redis,
            SecurityProperties properties
    ) {
        return new RedisSecHolderStrategy(om, redis, properties);
    }

}

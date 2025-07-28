package com.yangxj96.spectra.framework.configure;

import com.yangxj96.spectra.common.utils.HashUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;

/**
 * 缓存配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/25
 */
@Slf4j
@EnableCaching
@Configuration
public class CacheConfiguration {

    private static final String PREFIX = "[缓存配置]:";

    /**
     * Redis缓存管理器
     *
     * @return {@link CacheManager} 缓存管理器
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        log.atDebug().log(PREFIX + "Redis缓存管理器");
        var configuration = RedisCacheConfiguration
                // 载入默认配置
                .defaultCacheConfig()
                // 是否缓存null
                //.disableCachingNullValues()
                // 缓存分隔符
                .computePrefixWith(cacheName -> cacheName + ":")
                // Key使用string序列化
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                // Value使用jackson序列化
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(configuration)
                .build();
    }

    /**
     * 缓存key生成器
     *
     * @return {@link KeyGenerator}
     */
    @Bean
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getEnclosingClass() != null
                    ? target.getClass().getEnclosingClass().getSimpleName()
                    : target.getClass().getSimpleName());
            sb.append(":");
            sb.append(target.getClass().getSimpleName());
            sb.append(":");
            sb.append(method.getName());
            sb.append(":");

            // 把参数转换为字符串链接
            String paramKey = Arrays.toString(params);
            sb.append(paramKey);

            return HashUtils.md5(sb.toString());
        };
    }

}

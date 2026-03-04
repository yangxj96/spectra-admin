package com.devops00.spectra.core.configure.cache;


import com.devops00.spectra.common.constant.LogPrefix;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

/// 缓存配置
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/2/2 17:17
@Slf4j
@EnableCaching
@Configuration
public class CacheConfiguration {

    @Bean
    public RedisCacheManager redisCacheManager(ObjectMapper om, RedisConnectionFactory factory) {
        log.debug(LogPrefix.CACHE.f("配置RedisCacheManager"));
        // value 序列化（推荐 Jackson）
        var valueSerializer = new GenericJacksonJsonRedisSerializer(om);

        // key 序列化（String）
        var keyPair = RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer());

        RedisSerializationContext.SerializationPair<Object> valuePair = RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer);

        RedisCacheConfiguration defaultConfig =
                RedisCacheConfiguration.defaultCacheConfig()
                        .computePrefixWith(cacheName -> cacheName + ":")
                        .serializeKeysWith(keyPair)
                        .serializeValuesWith(valuePair)
                        .disableCachingNullValues()
                        .entryTtl(Duration.ofHours(1)); // 默认 TTL

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .build();
    }

}

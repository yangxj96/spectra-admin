package com.devops00.spectra.core.configure.cache;


import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.properties.SpectraSystemProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.Duration;

/// 缓存配置
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/2/2 17:17
@Slf4j
@EnableCaching
@Configuration
@RequiredArgsConstructor
public class CacheConfiguration {

    private final SpectraSystemProperties spectraSystemProperties;

    /// 缓存管理器
    @Bean
    public RedisCacheManager redisCacheManager(@Qualifier("redisObjectMapper") ObjectMapper om, RedisConnectionFactory factory) {
        log.debug(LogPrefix.CACHE.f("配置RedisCacheManager"));
        // value 序列化
        var valueSerializer = new JacksonJsonRedisSerializer<>(om, Object.class);

        // key 序列化（String）
        var keyPair = RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer());

        var valuePair = RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer);

        var defaultConfig =
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


    /// 定义一个redis专用的ObjectMapper
    @Bean("redisObjectMapper")
    public ObjectMapper redisObjectMapper(ObjectMapper om) {
        log.debug(LogPrefix.SERIALIZATION.f("开始配置缓存使用的ObjectMapper"));

        var ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(spectraSystemProperties.getPackagePrefix())
                .allowIfSubType("java.util")
                .allowIfSubType("java.time")
                .allowIfSubType("java.lang")
                .build();

        return om.rebuild()
                // 替代 serializationInclusion
                .changeDefaultPropertyInclusion(inclusion ->
                        inclusion.withValueInclusion(JsonInclude.Include.ALWAYS)
                )
                // 防止缓存结构变化炸掉
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                // 替代 activateDefaultTyping
                .activateDefaultTypingAsProperty(
                        ptv,
                        DefaultTyping.NON_FINAL_AND_RECORDS,
                        "@class"
                )
                .build();
    }

}

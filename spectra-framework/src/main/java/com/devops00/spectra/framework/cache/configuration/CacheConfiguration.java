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

package com.devops00.spectra.framework.cache.configuration;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.framework.cache.serialization.CacheValueRedisSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

/**
 * 缓存配置
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/2/2 17:17
 */
@Slf4j
@EnableCaching
@Configuration
public class CacheConfiguration {

    /**
     * 缓存管理器
     */
    @Bean
    public RedisCacheManager redisCacheManager(@Qualifier("redisObjectMapper") ObjectMapper om, RedisConnectionFactory factory) {
        log.debug(LogPrefix.CACHE.f("配置RedisCacheManager"));
        // value 序列化
        var valueSerializer = new CacheValueRedisSerializer(om);

        // key 序列化（String）
        var keyPair = RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer());

        var valuePair = RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer);

        var defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .computePrefixWith(cacheName -> cacheName + ":")
                .serializeKeysWith(keyPair)
                .serializeValuesWith(valuePair)
                .disableCachingNullValues()
                .entryTtl(Duration.ofHours(1)); // 默认 TTL

        return RedisCacheManager.builder(factory).cacheDefaults(defaultConfig).build();
    }

    /**
     * 定义一个redis专用的ObjectMapper
     */
    @Bean("redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        log.debug(LogPrefix.SERIALIZATION.f("开始配置缓存使用的ObjectMapper"));
        return JsonMapper.builder()
                .configureForJackson2()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }
}

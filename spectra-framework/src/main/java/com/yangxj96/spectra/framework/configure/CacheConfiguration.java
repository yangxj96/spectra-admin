package com.yangxj96.spectra.framework.configure;

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

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    private static final String PREFIX = "[CommonAutoConfiguration]:";

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

            String paramKey = Arrays.toString(params);
            String hash = md5Hash(paramKey);

            sb.append(hash);
            return sb.toString();
        };
    }

    // MD5 哈希方法
    private String md5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            BigInteger no = new BigInteger(1, messageDigest);
            StringBuilder hashText = new StringBuilder(no.toString(16));
            while (hashText.length() < 32) {
                hashText.insert(0, "0");
            }
            return hashText.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating MD5 hash", e);
        }
    }

}

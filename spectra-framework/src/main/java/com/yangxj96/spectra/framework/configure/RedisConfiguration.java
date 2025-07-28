package com.yangxj96.spectra.framework.configure;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/28
 */
@Slf4j
@Configuration
public class RedisConfiguration {

    private static final String PREFIX = "[Redis配置]:";


    @Resource
    private ObjectMapper om;

    /**
     * 自定义redisTemplate
     *
     * @param factory redis连接工程
     * @return RedisTemplate<String, Object>
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        log.atDebug().log(PREFIX + "开始配置Redis");
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 设置Key的序列化方式为String
        var keySerializer = new StringRedisSerializer();
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        // 使用Jackson作为Value的序列化方式
        var valueSerializer = new Jackson2JsonRedisSerializer<>(om, Object.class);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }


}

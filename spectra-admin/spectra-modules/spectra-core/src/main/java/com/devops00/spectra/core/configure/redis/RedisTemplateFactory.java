package com.devops00.spectra.core.configure.redis;


import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

/// Redis初始化工厂
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/15 14:55
public abstract class RedisTemplateFactory {

    private RedisTemplateFactory() {
    }

    public static RedisTemplate<String, Object> build(RedisConnectionFactory factory, ObjectMapper om) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        JacksonJsonRedisSerializer<Object> valueSerializer =
                new JacksonJsonRedisSerializer<>(om, Object.class);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }

}

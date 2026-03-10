package com.devops00.spectra.security.starter.configuration;


import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.security.starter.listener.SecurityRedisKeyExpirationListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

/// Security专用Redis配置
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/9 00:39
@Slf4j
public class SecRedisConfiguration {

    /// 自定义redisTemplate
    ///
    /// @param factory redis连接工程
    /// @return RedisTemplate<String, Object>
    @Bean("securityRedisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory, @Qualifier("securityObjectMapper") ObjectMapper om) {
        log.debug(LogPrefix.SECURITY.f("开始配置Security使用的RedisTemplate"));
        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(factory);

        var keySerializer = new StringRedisSerializer();
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        var valueSerializer =
                new JacksonJsonRedisSerializer<>(om, Object.class);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /// Security自定义Redis监听KEY事件
    @Bean
    public SecurityRedisKeyExpirationListener securityRedisKeyExpirationListener(@Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redis) {
        return new SecurityRedisKeyExpirationListener(redis);
    }

    /// Redis消息监听bean
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory factory, SecurityRedisKeyExpirationListener listener) {
        log.debug(LogPrefix.SECURITY.f("开始配置Redis值过期监听器"));
        var container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(
                listener,
                new PatternTopic("__keyevent@*__:expired")
        );
        return container;
    }


}

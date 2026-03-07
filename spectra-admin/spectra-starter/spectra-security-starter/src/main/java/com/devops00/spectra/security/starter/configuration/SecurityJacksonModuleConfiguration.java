package com.devops00.spectra.security.starter.configuration;


import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.properties.SpectraSystemProperties;
import com.devops00.spectra.security.starter.listener.SecurityRedisKeyExpirationListener;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.jackson.SecurityJacksonModules;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/// Security配置Jackson
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/15 14:42
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SecurityJacksonModules.class)
public class SecurityJacksonModuleConfiguration {

    @Resource
    private SpectraSystemProperties spectraSystemProperties;

    @Bean("securityObjectMapper")
    public ObjectMapper redisObjectMapper(ObjectMapper om) {
        log.debug(LogPrefix.SECURITY.f("开始配置Security使用的ObjectMapper"));
        return om.rebuild()
                .addModules(SecurityJacksonModules.getModules(getClass().getClassLoader(),
                        BasicPolymorphicTypeValidator.builder()
                                .allowIfSubType(spectraSystemProperties.getPackagePrefix())
                                .allowIfSubType("java.util")))
                .activateDefaultTyping(
                        BasicPolymorphicTypeValidator.builder()
                                .allowIfSubType(spectraSystemProperties.getPackagePrefix())
                                .allowIfSubType("java.util")
                                .build(),
                        DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY
                )
                .build();
    }

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

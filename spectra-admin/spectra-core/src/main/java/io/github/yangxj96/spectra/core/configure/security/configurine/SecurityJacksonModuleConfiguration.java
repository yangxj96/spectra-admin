package io.github.yangxj96.spectra.core.configure.security.configurine;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.yangxj96.spectra.core.configure.mvc.properties.SpectraSystemProperties;
import io.github.yangxj96.spectra.core.configure.redis.RedisTemplateFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.jackson.SecurityJacksonModules;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/**
 * Security配置Jackson
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/15 14:42
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(SecurityJacksonModules.class)
public class SecurityJacksonModuleConfiguration {

    private static final String PREFIX = "[Security]:";

    @Resource
    private SpectraSystemProperties spectraSystemProperties;

    @Bean("securityObjectMapper")
    public ObjectMapper redisObjectMapper(ObjectMapper om) {
        return om.rebuild()
                .addModules(SecurityJacksonModules.getModules(getClass().getClassLoader(),
                        BasicPolymorphicTypeValidator.builder()
                                .allowIfSubType("io.github.yangxj96.spectra")
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

    /**
     * 自定义redisTemplate
     *
     * @param factory redis连接工程
     * @return RedisTemplate<String, Object>
     */
    @Bean("securityRedisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory, @Qualifier("securityObjectMapper") ObjectMapper om) {
        log.debug(PREFIX + "开始配置Security使用的RedisTemplate");
        return RedisTemplateFactory.build(factory, om);
    }

}

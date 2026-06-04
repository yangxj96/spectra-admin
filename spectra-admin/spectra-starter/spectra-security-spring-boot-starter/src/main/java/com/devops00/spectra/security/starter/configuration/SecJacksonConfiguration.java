package com.devops00.spectra.security.starter.configuration;


import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.properties.SystemProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
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
public class SecJacksonConfiguration {

    @Resource
    private SystemProperties spectraSystemProperties;

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

}

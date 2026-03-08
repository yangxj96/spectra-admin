package com.devops00.spectra.security.starter.configuration;


import com.devops00.spectra.security.starter.advice.RestAccessDeniedHandler;
import com.devops00.spectra.security.starter.advice.RestAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

/// Security异常配置
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/9 00:35
public class SecExConfiguration {


    @Bean
    public AuthenticationEntryPoint restAuthenticationEntryPoint(@Qualifier("securityObjectMapper") ObjectMapper om) {
        return new RestAuthenticationEntryPoint(om);
    }

    @Bean
    public AccessDeniedHandler restAccessDeniedHandler(@Qualifier("securityObjectMapper") ObjectMapper om) {
        return new RestAccessDeniedHandler(om);
    }

}

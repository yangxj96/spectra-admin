package com.yangxj96.spectra.core.configuration.satoekn;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * SaToken相关配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Configuration
public class SaTokenConfiguration {


    /**
     * 密码加解密
     *
     * @return {@link BCryptPasswordEncoder}
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}

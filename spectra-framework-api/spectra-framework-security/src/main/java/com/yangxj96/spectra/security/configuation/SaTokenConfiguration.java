package com.yangxj96.spectra.security.configuation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * SaToken相关配置
 *
 * @author 杨新杰
 * @since 2025/5/26 17:45
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

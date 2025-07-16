package com.yangxj96.spectra.service.auth.configure;

import com.yangxj96.spectra.service.auth.properties.UserProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * SaToken配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(UserProperties.class)
public class SaTokenConfiguration {

    private static final String PREFIX = "[SaToken]:";

    /**
     * 密码加解密
     *
     * @return {@link BCryptPasswordEncoder}
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        log.atDebug().log(PREFIX + "加载密码加解密工具");
        return new BCryptPasswordEncoder();
    }
}

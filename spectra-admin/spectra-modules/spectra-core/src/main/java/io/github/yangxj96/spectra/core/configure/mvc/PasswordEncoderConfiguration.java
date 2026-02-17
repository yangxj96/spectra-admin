package io.github.yangxj96.spectra.core.configure.mvc;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/// 密码加密器
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/29 11:34
@Slf4j
@Configuration
public class PasswordEncoderConfiguration {

    private static final String PREFIX = "[Security]:";

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("{}配置PasswordEncoder", PREFIX);
        return new BCryptPasswordEncoder();
    }

}

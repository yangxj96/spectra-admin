package com.devops00.spectra.security.starter.autoconfiguration;


import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.starter.configuration.SecurityConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/// SpringSecurity配置
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/2 17:31
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(SecurityProperties.class)
@Import({SecurityConfiguration.class})
@ComponentScan("com.devops00.spectra.security.starter.web")
public class SecurityAutoConfiguration {


}

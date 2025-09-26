package io.github.yangxj96.spectra.license.configuration;

import io.github.yangxj96.spectra.license.properties.LicenseProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 许可相关配置
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(LicenseProperties.class)
public class LicenseConfiguration {
}

package com.devops00.spectra.upload.configure;


import com.devops00.spectra.upload.properties.LocalProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 本地上传配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/3/31 13:59
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(LocalProperties.class)
public class LocalConfiguration {

    private final LocalProperties properties;

}

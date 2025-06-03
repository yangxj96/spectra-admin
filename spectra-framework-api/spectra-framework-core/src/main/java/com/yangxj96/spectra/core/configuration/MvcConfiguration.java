package com.yangxj96.spectra.core.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * mvc配置
 *
 * @author 杨新杰
 * @since 2025/6/3 17:59
 */
@Configuration
public class MvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
                // 匹配所有路径
                .addMapping("/**")
                // 指定允许的源
                .allowedOrigins("http://localhost:5173")
                // 允许的方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的头部信息
                .allowedHeaders("*")
                // 是否支持凭证
                .allowCredentials(true);
    }
}

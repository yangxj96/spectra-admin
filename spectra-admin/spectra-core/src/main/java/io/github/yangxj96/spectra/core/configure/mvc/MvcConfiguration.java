/*
 *  Copyright 2018-2025 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.github.yangxj96.spectra.core.configure.mvc;

import io.github.yangxj96.spectra.core.configure.mvc.properties.CorsProperties;
import io.github.yangxj96.spectra.core.configure.mvc.properties.MvcProperties;
import io.github.yangxj96.spectra.core.configure.system.SpectraSystemProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * mvc配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({MvcProperties.class, SpectraSystemProperties.class, CorsProperties.class})
public class MvcConfiguration implements WebMvcConfigurer {

    private static final String PREFIX = "[SpringMVC]:";

    @Resource
    private CorsProperties corsProperties;

    @Resource
    private MvcProperties mvcProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.debug(PREFIX + "载入Cors");
        registry
                // 匹配所有路径
                .addMapping(corsProperties.getMapping())
                // 指定允许的源
                // .allowedOrigins("http://localhost:5173")
                .allowedOriginPatterns(corsProperties.getOriginPatterns().toArray(new String[0]))
                // 允许的方法
                // .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedMethods(corsProperties.getMethods().toArray(new String[0]))
                // 允许的头部信息
                .allowedHeaders(corsProperties.getHeaders().toArray(new String[0]))
                // 是否支持凭证
                .allowCredentials(corsProperties.getCredentials())
                // 预检后缓存策略时长
                .maxAge(corsProperties.getMaxAge());
    }

    /**
     * api版本策略配置
     *
     * @param configurer {@link ApiVersionConfigurer}
     */
    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        log.debug(
                "{}配置API版本号,默认请求头为{},默认版本号为{}",
                PREFIX,
                mvcProperties.getApiHeader(),
                mvcProperties.getApiVersion()
        );
        configurer
                .useRequestHeader(mvcProperties.getApiHeader())
                .setDefaultVersion(mvcProperties.getApiVersion())
                .detectSupportedVersions(true);
    }
}

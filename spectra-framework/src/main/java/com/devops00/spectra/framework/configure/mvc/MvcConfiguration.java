/*
 *  Copyright 2018-2026 yangxj96
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

package com.devops00.spectra.framework.configure.mvc;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.properties.SystemProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.URI;
import java.util.List;

/**
 * mvc配置
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(SystemProperties.class)
@RequiredArgsConstructor
public class MvcConfiguration implements WebMvcConfigurer {

    private final SystemProperties spectraProperties;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.debug(LogPrefix.WEB.f("载入Cors"));
        List<String> origins = spectraProperties.getCors()
                .getOriginPatterns()
                .stream()
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        if (origins.stream().anyMatch(origin -> origin.contains("*"))) {
            throw new IllegalStateException("CORS 仅允许精确 Origin，禁止通配符配置");
        }
        if (origins.isEmpty()) {
            if (spectraProperties.getCors().isRequired()) {
                throw new IllegalStateException("生产 CORS 必须显式配置精确 Origin allowlist");
            }
            log.warn("{}未配置 CORS Origin，跨源访问已关闭", LogPrefix.WEB.p());
            return;
        }
        origins.forEach(MvcConfiguration::validateOrigin);
        registry
                // 匹配所有路径
                .addMapping(spectraProperties.getCors().getMapping())
                // 只允许部署配置明确列出的 Origin；不使用 allowedOriginPatterns
                .allowedOrigins(origins.toArray(new String[0]))
                // 允许的方法
                .allowedMethods(spectraProperties.getCors().getMethods().toArray(new String[0]))
                // 允许的头部信息
                .allowedHeaders(spectraProperties.getCors().getHeaders().toArray(new String[0]))
                .exposedHeaders(HttpHeaders.CONTENT_DISPOSITION)
                // 是否支持凭证
                .allowCredentials(Boolean.TRUE.equals(spectraProperties.getCors().getCredentials()))
                // 预检后缓存策略时长
                .maxAge(spectraProperties.getCors().getMaxAge());
    }

    /**
     * 校验并确保数据满足当前约束（{@code validateOrigin}）。
     */
    private static void validateOrigin(String origin) {
        try {
            URI parsed = URI.create(origin);
            String scheme = parsed.getScheme();
            if (!("https".equalsIgnoreCase(scheme)
                    || ("http".equalsIgnoreCase(scheme) && isLoopback(parsed.getHost())))
                    || parsed.getHost() == null
                    || parsed.getHost().isBlank()
                    || parsed.getUserInfo() != null
                    || parsed.getPath() != null && !parsed.getPath().isEmpty()
                    || parsed.getQuery() != null
                    || parsed.getFragment() != null) {
                throw new IllegalStateException("CORS Origin 必须为 HTTPS 精确 Origin；开发环境仅允许 HTTP loopback: " + origin);
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("CORS Origin 格式无效: " + origin, exception);
        }
    }

    /**
     * 判断条件是否满足（{@code isLoopback}）。
     */
    private static boolean isLoopback(String host) {
        return "localhost".equalsIgnoreCase(host)
                || "127.0.0.1".equals(host)
                || "[::1]".equalsIgnoreCase(host)
                || "::1".equals(host);
    }

    @Override
    public void configureApiVersioning(@NonNull ApiVersionConfigurer configurer) {
        log.debug("{}配置API版本号,默认请求头为{},默认版本号为{}", LogPrefix.WEB.p(), spectraProperties.getMvc().getApiHeader(),
                spectraProperties.getMvc().getApiVersion());
        configurer.useRequestHeader(spectraProperties.getMvc().getApiHeader())
                .setDefaultVersion(spectraProperties.getMvc().getApiVersion())
                .detectSupportedVersions(true);
    }
}

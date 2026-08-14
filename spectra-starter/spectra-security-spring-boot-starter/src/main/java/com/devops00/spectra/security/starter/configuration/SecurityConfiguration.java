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

package com.devops00.spectra.security.starter.configuration;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.security.base.change.SecurityUserLookupPort;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.base.root.RootAuthorizationPolicy;
import com.devops00.spectra.security.starter.eval.SpectraPermissionEvaluator;
import com.devops00.spectra.security.starter.filter.TokenAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/**
 * Security功能配置
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/3/9 00:35
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Import({SecJacksonConfiguration.class, SecRedisConfiguration.class, SecExConfiguration.class, SecuritySessionPortConfiguration.class})
public class SecurityConfiguration {

    private final SecurityProperties properties;

    private final RootAuthorizationPolicy rootAuthorizationPolicy;

    private final AuthenticationEntryPoint restAuthenticationEntryPoint;

    private final AccessDeniedHandler restAccessDeniedHandler;

    /**
     * SpringSecurity 自定义的权限评估器
     */
    @Bean
    public SpectraPermissionEvaluator spectraPermissionEvaluator() {
        return new SpectraPermissionEvaluator(rootAuthorizationPolicy);
    }

    /**
     * 主认证管理器
     */
    @Bean
    @Primary
    public AuthenticationManager authenticationManager(ObjectProvider<AuthenticationProvider> providersProvider) {
        List<AuthenticationProvider> providers = providersProvider.orderedStream().toList();
        log.debug("{}配置AuthenticationManager,providers: {}", LogPrefix.SECURITY.p(), providers.size());
        return new ProviderManager(providers);
    }

    /**
     * 注解方法中的EL表达式认证处理器
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        log.debug(LogPrefix.SECURITY.f("开启注解方法中的EL表达式认证处理器"));
        var handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(spectraPermissionEvaluator());
        return handler;
    }

    /**
     * Spring Security核心过滤器
     *
     * @param http {@code HttpSecurity}
     * @return Security过滤器链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager,
                                                   SecurityContextAccessor securityContextAccessor,
                                                   SecurityUserLookupPort securityUserLookupPort) {
        log.debug(LogPrefix.SECURITY.f("配置核心过滤器"));

        // 白名单
        var whitelistPaths = properties.getWhitelists().toArray(new String[0]);
        log.debug("{}白名单:{}", LogPrefix.SECURITY.p(), whitelistPaths);
        log.debug(LogPrefix.SECURITY.f("关闭所有自带的认证方式,开放OPTIONS预检请求,开放白名单,其他接口全认证"));
        log.debug(LogPrefix.SECURITY.f("使用自定义的AuthenticationManager"));
        http.authenticationManager(authenticationManager)
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                // 安全起见关闭所有自带登录和退出方案
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // 默认没这个依赖,不能配置这个,会导致异常
                // .oauth2Login(AbstractHttpConfigurer::disable)
                .rememberMe(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                // SESSION 规则
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 注册过滤器
                .addFilterBefore(new TokenAuthenticationFilter(securityContextAccessor, securityUserLookupPort),
                        UsernamePasswordAuthenticationFilter.class)
                // 允许同源iframe
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                // 权限匹配
                .authorizeHttpRequests(auth -> auth
                        // 允许 ASYNC 调度
                        .dispatcherTypeMatchers(DispatcherType.ASYNC)
                        .permitAll()
                        // 预检请求必须放行
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()
                        // 白名单路径放行
                        .requestMatchers(whitelistPaths)
                        .permitAll()
                        // 其余接口都需要认证
                        .anyRequest()
                        .authenticated());

        log.debug(LogPrefix.SECURITY.f("异常处理"));
        http.exceptionHandling(ex -> ex.authenticationEntryPoint(restAuthenticationEntryPoint).accessDeniedHandler(restAccessDeniedHandler));

        return http.build();
    }
}

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

package com.devops00.spectra.framework.security.configuration.authentication;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.port.security.SecurityUserLookupPort;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.security.authorization.RootAuthorizationPolicy;
import com.devops00.spectra.framework.security.authorization.SpectraPermissionEvaluator;
import com.devops00.spectra.framework.security.authentication.TokenAuthenticationFilter;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.ratelimit.RedisRateLimiter;
import com.devops00.spectra.framework.security.ratelimit.RequestRateLimitFilter;
import com.devops00.spectra.framework.web.security.WebCookieCsrfFilter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

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
public class SecurityConfiguration {

    private final SecurityProperties properties;

    private final RootAuthorizationPolicy rootAuthorizationPolicy;

    private final AuthenticationEntryPoint restAuthenticationEntryPoint;

    private final AccessDeniedHandler restAccessDeniedHandler;

    /**
     * SpringSecurity 自定义的权限评估器
     *
     * @return 返回支持 Root 权限和通配权限匹配的 PermissionEvaluator Bean；Bean 创建失败时启动失败，不返回 null。
     */
    @Bean
    public SpectraPermissionEvaluator spectraPermissionEvaluator() {
        return new SpectraPermissionEvaluator(rootAuthorizationPolicy);
    }

    /**
     * 主认证管理器
     *
     * @param providersProvider Spring 容器中按顺序注册的认证 Provider 集合，用于构建统一认证管理器。
     * @return 返回由已注册 AuthenticationProvider 组成的认证管理器；没有可用 Provider 或构建失败时启动失败，不返回 null。
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
     *
     * @return 返回接入项目 PermissionEvaluator 的方法安全表达式处理器；Bean 创建失败时启动失败，不返回 null。
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        log.debug(LogPrefix.SECURITY.f("开启注解方法中的EL表达式认证处理器"));
        var handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(spectraPermissionEvaluator());
        return handler;
    }

    /**
     * Redis API 限流器。
     *
     * @param redis 安全 Redis Template，用于执行限流计数和窗口过期操作。
     * @return 返回使用安全 Redis 执行原子计数的 API 限流器；Bean 创建失败时启动失败，不返回 null。
     */
    @Bean
    public RedisRateLimiter redisRateLimiter(@Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redis) {
        return new RedisRateLimiter(redis);
    }

    /**
     * Spring Security核心过滤器
     *
     * @param http                    {@code HttpSecurity} 构建器，用于声明无状态认证和请求授权规则。
     * @param authenticationManager   执行登录凭据认证的统一认证管理器。
     * @param securityContextAccessor 安全上下文访问器，用于取得当前操作者并填充持久化审计字段。
     * @param securityUserLookupPort  按访问令牌读取当前用户资料的端口。
     * @param redisRateLimiter        执行 API 限流判定的安全 Redis 限流器。
     * @param objectMapper            安全请求日志和限流响应使用的受控 JSON 映射器。
     * @param meterRegistryProvider   提供限流指标注册表；没有外部注册表时使用临时注册表。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager,
                                                   SecurityContextAccessor securityContextAccessor,
                                                   SecurityUserLookupPort securityUserLookupPort,
                                                   RedisRateLimiter redisRateLimiter,
                                                   @Qualifier("securityObjectMapper") ObjectMapper objectMapper,
                                                   ObjectProvider<MeterRegistry> meterRegistryProvider) {
        log.debug(LogPrefix.SECURITY.f("配置核心过滤器"));

        MeterRegistry meterRegistry = meterRegistryProvider.getIfAvailable(SimpleMeterRegistry::new);
        var requestRateLimitFilter = new RequestRateLimitFilter(redisRateLimiter, securityContextAccessor,
                objectMapper, meterRegistry);

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
                .addFilterBefore(new WebCookieCsrfFilter(properties, restAccessDeniedHandler),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new TokenAuthenticationFilter(securityContextAccessor, securityUserLookupPort),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(requestRateLimitFilter, TokenAuthenticationFilter.class)
                // 允许同源iframe
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                // 权限匹配
                .authorizeHttpRequests(auth -> auth
                        .withObjectPostProcessor(new ObjectPostProcessor<AuthorizationFilter>() {
                            /**
                             * 为安全 Bean 后处理器注册必要的生命周期处理。
                             *
                             * @param filter Spring Security 授权过滤器实例，将被补充异步 dispatch 处理配置。
                             * @return 返回完成项目安全异常处理器注入后的原过滤器；过滤器对象始终原样返回，不返回 null。
                             */
                            @Override
                            public <O extends AuthorizationFilter> O postProcess(O filter) {
                                filter.setFilterAsyncDispatch(true);
                                return filter;
                            }
                        })
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

package io.github.yangxj96.spectra.security.starter.autoconfiguration;


import io.github.yangxj96.spectra.common.constant.LogPrefix;
import io.github.yangxj96.spectra.security.base.properties.SecurityProperties;
import io.github.yangxj96.spectra.security.starter.eval.SpectraPermissionEvaluator;
import io.github.yangxj96.spectra.security.starter.advice.RestAccessDeniedHandler;
import io.github.yangxj96.spectra.security.starter.advice.RestAuthenticationEntryPoint;
import io.github.yangxj96.spectra.security.starter.filter.TokenAuthenticationFilter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

/// SpringSecurity配置
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/2 17:31
@Slf4j
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityAutoConfiguration {

    @Resource
    private SecurityProperties properties;

    @Resource
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @Resource
    private SpectraPermissionEvaluator spectraPermissionEvaluator;

    @Resource
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Resource
    private RestAccessDeniedHandler restAccessDeniedHandler;

    @Resource
    private List<AuthenticationProvider> providers;


    @Bean
    public AuthenticationManager authenticationManager() {
        log.debug(LogPrefix.SECURITY.f("配置AuthenticationManager"));
        return new ProviderManager(providers);
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        log.debug(LogPrefix.SECURITY.f("开启注解方法中的EL表达式认证处理器"));
        var handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(spectraPermissionEvaluator);
        return handler;
    }

    /// Spring Security核心过滤器
    ///
    /// @param http `HttpSecurity`
    /// @return Security过滤器链
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        log.debug(LogPrefix.SECURITY.f("配置核心过滤器"));
        // 白名单
        var whitelistPaths = properties.getWhitelists().toArray(new String[0]);

        log.debug("{}白名单:{}", LogPrefix.SECURITY.p(), whitelistPaths);
        log.debug(LogPrefix.SECURITY.f("关闭所有自带的认证方式,开放OPTIONS预检请求,开放白名单,其他接口全认证"));
        http
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
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 权限匹配
                .authorizeHttpRequests(auth ->
                        auth
                                // 预检请求必须放行
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                // 白名单路径放行
                                .requestMatchers(whitelistPaths).permitAll()
                                // 其余接口都需要认证
                                .anyRequest().authenticated())
        ;

        log.debug(LogPrefix.SECURITY.f("异常处理"));
        http.exceptionHandling(ex ->
                ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
        );

        return http.build();
    }


}

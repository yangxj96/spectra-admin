package io.github.yangxj96.spectra.security.web;

import io.github.yangxj96.spectra.security.api.properties.SecurityProperties;
import io.github.yangxj96.spectra.security.web.exception.RestAccessDeniedHandler;
import io.github.yangxj96.spectra.security.web.exception.RestAuthenticationEntryPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@ComponentScan("io.github.yangxj96.spectra.security")
public class WebSecurityAutoConfiguration {

    private static final String PREFIX = "[Security]:";

    private final SecurityProperties properties;

    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    private final RestAccessDeniedHandler restAccessDeniedHandler;

    public WebSecurityAutoConfiguration(SecurityProperties properties,
                                        RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                                        RestAccessDeniedHandler restAccessDeniedHandler) {
        this.properties = properties;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        this.restAccessDeniedHandler = restAccessDeniedHandler;
    }


    /// Spring Security核心过滤器
    ///
    /// @param http `HttpSecurity`
    /// @return Security过滤器链
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        log.debug("{}配置核心过滤器", PREFIX);
        // 白名单
        var whitelistPaths = properties.getWhitelists().toArray(new String[0]);

        log.debug("{}白名单:{}", PREFIX, whitelistPaths);
        log.debug("{}关闭所有自带的认证方式,开放OPTIONS预检请求,开放白名单,其他接口全认证", PREFIX);
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
                // .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                // 权限匹配
                .authorizeHttpRequests(authz ->
                        authz
                                // 预检请求必须放行
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                // 白名单路径放行
                                .requestMatchers(whitelistPaths).permitAll()
                                // 其余接口都需要认证
                                .anyRequest().authenticated())
        ;

        log.debug("{}异常处理", PREFIX);
        http.exceptionHandling(ex ->
                ex
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler)
        );

        return http.build();
    }


}

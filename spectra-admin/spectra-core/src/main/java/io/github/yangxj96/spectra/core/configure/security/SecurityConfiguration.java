package io.github.yangxj96.spectra.core.configure.security;


import io.github.yangxj96.spectra.core.configure.security.eval.SpectraPermissionEvaluator;
import io.github.yangxj96.spectra.core.configure.security.exception.RestAccessDeniedHandler;
import io.github.yangxj96.spectra.core.configure.security.exception.RestAuthenticationEntryPoint;
import io.github.yangxj96.spectra.core.configure.security.filter.TokenAuthenticationFilter;
import io.github.yangxj96.spectra.core.configure.security.properties.SecurityProperties;
import io.github.yangxj96.spectra.core.configure.system.SpectraSystemProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/**
 * SpringSecurity配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/2 17:31
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfiguration {

    private static final String PREFIX = "[Security]:";

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
    private ObjectMapper om;

    @Resource
    private SpectraSystemProperties spectraSystemProperties;

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.debug("{}配置PasswordEncoder", PREFIX);
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        log.debug("{}配置AuthenticationManager", PREFIX);
        return config.getAuthenticationManager();
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        log.debug("{}开启注解方法中的EL表达式认证处理器", PREFIX);
        var handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(spectraPermissionEvaluator);
        return handler;
    }

    /**
     * Spring Security核心过滤器
     *
     * @param http {@link HttpSecurity}
     * @return {@link SecurityFilterChain}
     */
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
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
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

    /**
     * Security专用的ObjectMapper
     *
     * @return
     */
    @Bean("securityObjectMapper")
    public ObjectMapper securityObjectMapper() {
        log.debug(PREFIX + "开始配置专用ObjectMapper");

        var validator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(spectraSystemProperties.getPackagePrefix());

        return om.rebuild()
                // 注册Security的modules
                .addModules(SecurityJacksonModules.getModules(getClass().getClassLoader(), validator))
                .enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
                .build();
    }

    /**
     * Security专用的RedisTemplate
     *
     * @param factory redis连接工程
     * @return RedisTemplate<String, Object>
     */
    @Bean("securityRedisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory, @Qualifier("securityObjectMapper") ObjectMapper om) {
        log.debug(PREFIX + "开始配置专用Redis");
        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(factory);

        // 设置Key的序列化方式为String
        var keySerializer = new StringRedisSerializer();
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        // 使用Jackson作为Value的序列化方式
        var valueSerializer = new JacksonJsonRedisSerializer<>(om, Object.class);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }
}

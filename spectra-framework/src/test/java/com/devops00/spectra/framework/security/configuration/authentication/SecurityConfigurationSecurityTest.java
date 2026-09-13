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

import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.port.security.SecurityUserLookupPort;
import com.devops00.spectra.common.security.authorization.RootAuthorizationPolicy;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.ratelimit.RedisRateLimiter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.data.redis.core.RedisTemplate;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Spring Security Web 边界回归测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class SecurityConfigurationSecurityTest {

    @Test
    void shouldRequireAuthenticationForAnonymousAsyncDispatch() throws Exception {
        var chain = buildChain();
        var request = new MockHttpServletRequest("GET", "/api/system/private");
        request.setDispatcherType(DispatcherType.ASYNC);
        var response = new MockHttpServletResponse();

        new org.springframework.security.web.FilterChainProxy(chain)
                .doFilter(request, response, terminalChain());

        assertEquals(401, response.getStatus());
    }

    @Test
    void shouldSendSameOriginFrameHeaderForSameOriginEmbedding() throws Exception {
        var chain = buildChain();
        var request = new MockHttpServletRequest("GET", "/api/system/bootstrap");
        var response = new MockHttpServletResponse();

        new org.springframework.security.web.FilterChainProxy(chain)
                .doFilter(request, response, terminalChain());

        assertEquals("SAMEORIGIN", response.getHeader("X-Frame-Options"));
    }

    /**
     * 构建安全配置安全。
     */
    @SuppressWarnings("unchecked")
    private static List<SecurityFilterChain> buildChain() throws Exception {
        ObjectPostProcessor<Object> postProcessor = new ObjectPostProcessor<>() {
            /** {@inheritDoc} */
            @Override
            public <O extends Object> O postProcess(O object) {
                return object;
            }
        };
        var http = new HttpSecurity(postProcessor, new AuthenticationManagerBuilder(postProcessor), new HashMap<>());
        var applicationContext = new GenericApplicationContext();
        applicationContext.registerBean("corsConfigurationSource", UrlBasedCorsConfigurationSource.class,
                (Supplier<UrlBasedCorsConfigurationSource>) UrlBasedCorsConfigurationSource::new);
        applicationContext.refresh();
        http.setSharedObject(org.springframework.context.ApplicationContext.class, applicationContext);
        http.setSharedObject(CorsConfigurationSource.class, new UrlBasedCorsConfigurationSource());
        var authenticationEntryPoint = (org.springframework.security.web.AuthenticationEntryPoint) (request, response, exception) -> response
                .setStatus(401);
        var accessDeniedHandler = (AccessDeniedHandler) (request, response, exception) -> response.setStatus(403);
        var configuration = new SecurityConfiguration(new SecurityProperties(), mock(RootAuthorizationPolicy.class),
                authenticationEntryPoint, accessDeniedHandler);
        var objectProvider = mock(ObjectProvider.class);
        when(objectProvider.getIfAvailable(org.mockito.ArgumentMatchers.any(Supplier.class)))
                .thenReturn(new SimpleMeterRegistry());
        RedisTemplate<String, Object> redis = (RedisTemplate<String, Object>) mock(RedisTemplate.class);
        return List.of(configuration.securityFilterChain(http, mock(AuthenticationManager.class),
                mock(SecurityContextAccessor.class), mock(SecurityUserLookupPort.class),
                new RedisRateLimiter(redis), new ObjectMapper(), objectProvider));
    }

    /**
     * 处理安全配置安全相关数据。
     */
    private static FilterChain terminalChain() {
        return (request, response) -> {
            if (response instanceof MockHttpServletResponse mockResponse) {
                mockResponse.setStatus(200);
            }
        };
    }
}

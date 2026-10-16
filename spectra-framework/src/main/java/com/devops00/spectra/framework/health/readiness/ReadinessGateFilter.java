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

package com.devops00.spectra.framework.health.readiness;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 运行期就绪门：依赖未就绪时拒绝业务请求。
 *
 * <p>数据库或安全 Redis 运行故障使运行态转为 UNREADY 后，所有非探测业务请求返回 503，
 * 与认证失败区分；健康探测路径和预检请求放行，便于探活与恢复判断。进程保持存活。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@Component
@NullMarked
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReadinessGateFilter extends OncePerRequestFilter {

    private static final String UNAVAILABLE_BODY = "{\"code\":503,\"msg\":\"服务暂不可用\"}";

    private final DependencyReadinessState state;

    public ReadinessGateFilter(DependencyReadinessState state) {
        this.state = state;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (state.isReady() || isProbeOrPreflight(request)) {
            chain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(UNAVAILABLE_BODY);
    }

    private static boolean isProbeOrPreflight(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        return request.getRequestURI().contains("/actuator/");
    }
}

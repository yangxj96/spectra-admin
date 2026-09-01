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

package com.devops00.spectra.framework.configure.mvc.filter;

import com.devops00.spectra.common.audit.RequestCorrelationContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 统一 HTTP 请求链路标识过滤器。
 *
 * <p>只接受受限字符集的外部 ID；非法值不会进入响应头、MDC、审计或业务事件。请求结束后恢复线程上下文并清理
 * MDC，避免虚拟线程或容器线程复用时发生链路串线。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/1
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestCorrelationFilter extends OncePerRequestFilter {

    /** 外部请求 ID 请求头。 */
    public static final String REQUEST_ID_HEADER = RequestCorrelationContext.REQUEST_ID_HEADER;
    /** 外部关联 ID 请求头。 */
    public static final String CORRELATION_ID_HEADER = RequestCorrelationContext.CORRELATION_ID_HEADER;
    /** 清洗后请求 ID 的 request attribute。 */
    public static final String REQUEST_ID_ATTRIBUTE = RequestCorrelationContext.REQUEST_ID_ATTRIBUTE;
    /** 清洗后关联 ID 的 request attribute。 */
    public static final String CORRELATION_ID_ATTRIBUTE = RequestCorrelationContext.CORRELATION_ID_ATTRIBUTE;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        var context = RequestCorrelationContext.forHttp(
                request.getHeader(REQUEST_ID_HEADER), request.getHeader(CORRELATION_ID_HEADER));
        request.setAttribute(REQUEST_ID_ATTRIBUTE, context.requestId());
        request.setAttribute(CORRELATION_ID_ATTRIBUTE, context.correlationId());
        setResponseHeaders(response, context);
        try (var ignored = RequestCorrelationContext.openWithMdc(context)) {
            filterChain.doFilter(request, response);
        } finally {
            // 再次设置，确保下游没有覆盖为未经清洗的值；响应已提交时 Servlet 容器会按自身规则处理。
            setResponseHeaders(response, context);
        }
    }

    private void setResponseHeaders(HttpServletResponse response, RequestCorrelationContext.Context context) {
        response.setHeader(REQUEST_ID_HEADER, context.requestId());
        response.setHeader(CORRELATION_ID_HEADER, context.correlationId());
    }

}

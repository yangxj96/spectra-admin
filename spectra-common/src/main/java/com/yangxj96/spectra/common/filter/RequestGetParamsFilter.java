/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.CaseUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Get请求参数下滑先转驼峰命名
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
@Component
public class RequestGetParamsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        log.atDebug().log("开始处理请求参数下划线转小驼峰命名");
        if (!request.getMethod().toUpperCase(Locale.getDefault()).equals("GET")) {
            filterChain.doFilter(request, response);
            return;
        }
        var formatted = new ConcurrentHashMap<String, String[]>();
        for (String param : request.getParameterMap().keySet()) {
            var k = "";
            if (param.contains("_")) {
                // 原先的hutool,决定不在使用了
                // k = CharSequenceUtil.toCamelCase(param)
                k = CaseUtils.toCamelCase(param, false, '_');
            } else {
                k = param;
            }
            formatted.put(k, request.getParameterValues(param));
        }
        filterChain.doFilter(new ParamsModifyHttpServletRequestWrapper(request, formatted), response);
    }

    public static class ParamsModifyHttpServletRequestWrapper extends HttpServletRequestWrapper {

        private final ConcurrentMap<String, String[]> formatted;

        public ParamsModifyHttpServletRequestWrapper(HttpServletRequest request,
                                                     ConcurrentMap<String, String[]> formatted) {
            super(request);
            this.formatted = formatted;
        }

        @Override
        public String getParameter(String name) {
            if (formatted.containsKey(name)) {
                return formatted.get(name)[0];
            } else {
                return null;
            }
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(formatted.keySet());
        }

        @Override
        public String[] getParameterValues(String name) {
            if (formatted.containsKey(name)) {
                return formatted.get(name);
            } else {
                return new String[]{""};
            }
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return formatted;
        }
    }

}

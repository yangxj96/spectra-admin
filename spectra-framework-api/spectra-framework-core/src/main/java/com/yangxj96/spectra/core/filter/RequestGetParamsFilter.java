/*
 * Copyright (c) 2018-2025 Jack Young (杨新杰)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.yangxj96.spectra.core.filter;

import cn.hutool.core.text.CharSequenceUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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
 * @author 杨新杰
 * @since 2025/6/3 23:32
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
                k = CharSequenceUtil.toCamelCase(param);
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

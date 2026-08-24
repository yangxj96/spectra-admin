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

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.utils.StrUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Get请求参数下滑先转驼峰命名
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Slf4j
@Component
public class RequestGetParamsFilter extends OncePerRequestFilter {

    @Override
    @NullMarked
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.debug(LogPrefix.WEB.f("开始处理请求参数下划线转小驼峰命名"));
        if (!request.getMethod().toUpperCase(Locale.getDefault()).equals("GET")) {
            log.debug(LogPrefix.WEB.f("非GET方法,跳过"));
            filterChain.doFilter(request, response);
            return;
        }
        var formatted = new ConcurrentHashMap<String, String[]>();
        for (var param : request.getParameterMap().keySet()) {
            var k = "";
            if (param.contains("_")) {
                k = StrUtils.toCamelCase(param, false, '_');
            } else {
                k = param;
            }
            formatted.put(k, request.getParameterValues(param));
        }
        log.debug(LogPrefix.WEB.f("转换成功,继续往下执行"));
        filterChain.doFilter(new ParamsModifyHttpServletRequestWrapper(request, formatted), response);
    }

    public static class ParamsModifyHttpServletRequestWrapper extends HttpServletRequestWrapper {

        private final ConcurrentMap<String, String[]> formatted;

        public ParamsModifyHttpServletRequestWrapper(HttpServletRequest request, ConcurrentMap<String, String[]> formatted) {
            super(request);
            this.formatted = new ConcurrentHashMap<>(copyParameterMap(formatted));
        }

        @Override
        public @Nullable String getParameter(String name) {
            var values = formatted.get(name);
            return values == null || values.length == 0 ? null : values[0];
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(formatted.keySet());
        }

        @Override
        public String[] getParameterValues(String name) {
            var values = formatted.get(name);
            return values == null ? null : values.clone();
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return copyParameterMap(formatted);
        }

        /**
         * 转换、解析或规范化数据（{@code copyParameterMap}）。
         */
        private static Map<String, String[]> copyParameterMap(Map<String, String[]> source) {
            var copy = new HashMap<String, String[]>();
            source.forEach((key, values) -> copy.put(key, values == null ? null : values.clone()));
            return Collections.unmodifiableMap(copy);
        }
    }
}

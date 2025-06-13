package com.yangxj96.spectra.core.filter;

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

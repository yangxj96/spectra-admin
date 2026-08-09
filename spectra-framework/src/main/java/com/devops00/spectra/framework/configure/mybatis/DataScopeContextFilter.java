/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.framework.configure.mybatis;

import com.devops00.spectra.common.mybatis.DataScopeContextHolder;
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
 * 为每个 HTTP 请求建立并清理数据权限快照。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class DataScopeContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        DataScopeContextHolder.beginRequest();
        try {
            filterChain.doFilter(request, response);
        } finally {
            DataScopeContextHolder.endRequest();
        }
    }
}

/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.configure;

import com.devops00.spectra.core.security.authentication.util.AuthenticationWebUtils;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

/** Local 原始分片上传的 CSRF 边界。 */
@Component
public class FileUploadBinarySecurityFilter extends OncePerRequestFilter {

    private static final Pattern LOCAL_PART_PATH = Pattern.compile(
            ".*/file/uploads/[0-9a-fA-F-]{36}/parts/[1-9][0-9]*/content$");

    private final SecurityProperties securityProperties;

    public FileUploadBinarySecurityFilter(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if ("PUT".equalsIgnoreCase(request.getMethod()) && LOCAL_PART_PATH.matcher(request.getRequestURI()).matches()) {
            try {
                AuthenticationWebUtils.validateCsrf(request, securityProperties);
            } catch (IllegalArgumentException exception) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF 校验失败");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}

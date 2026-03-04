package com.devops00.spectra.security.starter.advice;


import com.devops00.spectra.common.response.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/// 权限错误处理
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/8 14:45
@Component
@NullMarked
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper om;

    public RestAccessDeniedHandler(@Qualifier("securityObjectMapper") ObjectMapper om) {
        this.om = om;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException e) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        var body = R.failure(HttpStatus.FORBIDDEN, "权限不足");

        response.getWriter().write(om.writeValueAsString(body));
    }

}

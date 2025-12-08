package io.github.yangxj96.spectra.core.configure.security.exception;


import io.github.yangxj96.spectra.common.response.R;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * 错误处理
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/8 14:45
 */
@Component
@NullMarked
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Resource
    private ObjectMapper om;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException e) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        var body = R.failure(HttpStatus.FORBIDDEN, "权限不足");

        response.getWriter().write(om.writeValueAsString(body));
    }

}

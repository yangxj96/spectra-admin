package io.github.yangxj96.spectra.core.configure.security.exception;


import io.github.yangxj96.spectra.common.response.R;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/// 认证错误处理
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/8 14:43
@Component
@NullMarked
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Resource
    private ObjectMapper om;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException e) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        // 按你项目的统一异常格式写出
        var body = om.writeValueAsString(R.failure(HttpStatus.UNAUTHORIZED, "用户未登录/登录失效"));
        response.getWriter().write(body);
    }

}

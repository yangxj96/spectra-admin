package io.github.yangxj96.spectra.security.starter.advice;


import io.github.yangxj96.spectra.common.response.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
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

    private final ObjectMapper om;

    public RestAuthenticationEntryPoint(@Qualifier("securityObjectMapper") ObjectMapper om) {
        this.om = om;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException e) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String message = switch (e) {
            case BadCredentialsException _ -> "账号或密码错误";
            case CredentialsExpiredException _ -> "登录已过期";
            case InsufficientAuthenticationException _ -> "用户未登录";
            default -> "认证失败";
        };

        var body = om.writeValueAsString(R.failure(HttpStatus.UNAUTHORIZED, message));
        response.getWriter().write(body);
    }

}

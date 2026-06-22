package com.devops00.spectra.security.starter.filter;


import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.security.base.holder.SecUtil;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/// token鉴权过滤器
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/2 23:45
@NullMarked
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String token = SecUtil.getCurrentToken();
        if (StrUtils.isNotBlank(token)) {
            SecurityUser user = SecUtil.getCurrentUser(token);
            if (user == null) {
                // token 解析异常（过期/篡改等）→ 清空上下文
                SecurityContextHolder.clearContext();
            } else {
                // 构建 Authentication 对象
                var auth = new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
                // 绑定到 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(request, response);
    }

}

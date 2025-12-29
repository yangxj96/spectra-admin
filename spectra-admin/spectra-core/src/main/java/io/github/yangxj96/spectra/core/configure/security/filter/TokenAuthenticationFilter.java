package io.github.yangxj96.spectra.core.configure.security.filter;


import io.github.yangxj96.spectra.common.utils.StrUtils;
import io.github.yangxj96.spectra.core.configure.security.holder.SecUtil;
import io.github.yangxj96.spectra.core.configure.security.javabean.SecurityUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * token鉴权
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/2 23:45
 */
@Component
@NullMarked
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String token = SecUtil.getCurrentToken();
        if (StrUtils.isNotBlank(token)) {
            SecurityUser user = SecUtil.getCurrentUser(token);
            if (user == null) {
                // token 无效
                throw new InsufficientAuthenticationException("Token 无效或已过期");
            }
            // 构建 Authentication 对象
            var auth = new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
            // 绑定到 SecurityContext
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(request, response);
    }

}

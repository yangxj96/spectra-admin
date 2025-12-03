package io.github.yangxj96.spectra.core.configure.security.filter;


import io.github.yangxj96.spectra.core.javabean.auth.SecurityUser;
import io.github.yangxj96.spectra.core.service.auth.TokenService;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
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
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @Resource
    private TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null) {
            SecurityUser user = tokenService.getUserByToken(token);
            if (user != null) {
                // 构建 Authentication 对象
                var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                // 绑定到 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        chain.doFilter(request, response);
    }

    private @Nullable String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}

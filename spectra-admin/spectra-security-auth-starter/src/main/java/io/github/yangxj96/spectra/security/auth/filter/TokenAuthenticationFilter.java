package io.github.yangxj96.spectra.security.auth.filter;

import io.github.yangxj96.spectra.security.auth.core.TokenAuthentication;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1️⃣ 从请求中获取 token（你自己的规则）
        // TODO: 从 Header / Cookie / 参数中获取 token
        String token = resolveToken(request);

        if (token != null) {
            // 2️⃣ 校验 token（留空）
            // TODO: 校验 token 有效性（签名 / 过期 / 黑名单）

            // 3️⃣ 解析用户信息（留空）
            // TODO: 从 token 中解析用户信息
            Object principal = parsePrincipal(token);

            // 4️⃣ 构造 Authentication
            TokenAuthentication authentication =
                    new TokenAuthentication(
                            principal,
                            token,
                            Collections.emptyList()
                    );

            // 5️⃣ 放入 SecurityContext
            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // TODO: 实现你自己的 token 获取方式
        return null;
    }

    private Object parsePrincipal(String token) {
        // TODO: 从 token 中解析用户信息
        return null;
    }
}

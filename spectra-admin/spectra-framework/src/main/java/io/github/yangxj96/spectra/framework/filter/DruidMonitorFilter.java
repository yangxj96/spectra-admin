package io.github.yangxj96.spectra.framework.filter;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.yangxj96.spectra.common.response.R;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;

/**
 * druid连接池监控页面配置
 */
@Slf4j
public record DruidMonitorFilter(ObjectMapper om) implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 1. 检查是否登录
        if (!StpUtil.isLogin()) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write(om.writeValueAsString(R.failure(HttpStatus.UNAUTHORIZED, "未登录")));
            return;
        }

        // 2. 检查角色（例如：必须是 admin）
        if (!StpUtil.hasRole("admin")) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write(om.writeValueAsString(R.failure(HttpStatus.UNAUTHORIZED, "权限不足")));
            return;
        }

        // 3. 通过，放行
        chain.doFilter(request, response);
    }

}

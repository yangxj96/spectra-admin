package io.github.yangxj96.spectra.security.method.authorization;

import io.github.yangxj96.spectra.security.api.annotation.HasPermission;
import io.github.yangxj96.spectra.security.api.checker.PermissionChecker;
import io.github.yangxj96.spectra.security.api.context.SecurityContext;
import io.github.yangxj96.spectra.security.method.context.SpringSecurityContextAdapter;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public class HasPermissionAuthorizationManager implements AuthorizationManager<MethodInvocation> {

    private final PermissionChecker permissionChecker;

    public HasPermissionAuthorizationManager(PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;
    }

    @Override
    public AuthorizationDecision authorize(
            Supplier<? extends Authentication> authenticationSupplier,
            MethodInvocation invocation
    ) {
        // 1. 找到 @HasPermission 注解
        HasPermission hasPermission = findAnnotation(invocation);
        if (hasPermission == null) {
            return new AuthorizationDecision(true); // 没注解直接放行
        }

        // 2. 获取认证对象
        Authentication auth = authenticationSupplier.get();
        if (auth == null) {
            auth = SecurityContextHolder.getContext().getAuthentication();
        }

        // 3. 转成自定义 SecurityContext（可选）
        var ctx = auth != null ? new SpringSecurityContextAdapter(auth) : null;

        // 4. 调用 PermissionChecker
        boolean granted = ctx != null
                ? permissionChecker.hasPermission(ctx, hasPermission.value())  // 如果你的 PermissionChecker 接收 Authentication，可以直接传 auth
                : false; // 未认证则拒绝

        return new AuthorizationDecision(granted);
    }

    private HasPermission findAnnotation(MethodInvocation invocation) {
        Method method = invocation.getMethod();
        HasPermission ann = AnnotationUtils.findAnnotation(method, HasPermission.class);
        if (ann != null) return ann;

        return AnnotationUtils.findAnnotation(invocation.getThis().getClass(), HasPermission.class);
    }
}

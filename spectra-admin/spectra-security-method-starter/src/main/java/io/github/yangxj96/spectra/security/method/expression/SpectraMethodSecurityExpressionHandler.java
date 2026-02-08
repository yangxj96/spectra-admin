package io.github.yangxj96.spectra.security.method.expression;

import io.github.yangxj96.spectra.security.api.checker.PermissionChecker;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class SpectraMethodSecurityExpressionHandler
        extends DefaultMethodSecurityExpressionHandler {

    private final PermissionChecker permissionChecker;

    public SpectraMethodSecurityExpressionHandler(
            ApplicationContext context,
            PermissionChecker permissionChecker
    ) {
        setApplicationContext(context);
        this.permissionChecker = permissionChecker;
    }

    @Override
    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(
            Authentication authentication,
            MethodInvocation invocation) {
        var root = new SpectraMethodSecurityExpressionRoot(authentication, permissionChecker);
        root.setThis(invocation.getThis());
        return root;
    }
}

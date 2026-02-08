package io.github.yangxj96.spectra.security.method.expression;

import io.github.yangxj96.spectra.security.api.checker.PermissionChecker;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class SpectraMethodSecurityExpressionRoot implements MethodSecurityExpressionOperations {

    private final Authentication authentication;
    private final PermissionChecker permissionChecker;

    private Object filterObject;
    private Object returnObject;
    private Object target;

    public SpectraMethodSecurityExpressionRoot(Authentication authentication, PermissionChecker permissionChecker) {
        this.authentication = authentication;
        this.permissionChecker = permissionChecker;
    }

    public boolean hasPermission(String permission) {
        return permissionChecker.hasPermission(authentication, permission);
    }

    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return this.filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return this.returnObject;
    }

    @Override
    public Object getThis() {
        return this.target;
    }

    public void setThis(Object target) {
        this.target = target;
    }
}

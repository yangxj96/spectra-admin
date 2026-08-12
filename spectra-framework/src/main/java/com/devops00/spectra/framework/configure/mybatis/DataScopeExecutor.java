/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.framework.configure.mybatis;

import com.devops00.spectra.common.exception.DataScopeViolationException;
import com.devops00.spectra.common.mybatis.DataScopeContextHolder;
import com.devops00.spectra.security.base.holder.SecUtil;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * 受控的数据隔离手动扩展点。
 * <p>
 * 只有系统运维角色或显式通配权限可以临时绕过隔离，调用范围限定在一个
 * lambda 内，避免出现全局开关未恢复的问题。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Component
public class DataScopeExecutor {

    /**
     * 在系统运维权限校验通过后执行有返回值的隔离绕过任务。
     */
    public <T> T withoutScope(Supplier<T> action) {
        requireSystemOperator();
        return DataScopeContextHolder.withBypass(action);
    }

    /**
     * 在系统运维权限校验通过后执行无返回值的隔离绕过任务。
     */
    public void withoutScope(Runnable action) {
        requireSystemOperator();
        DataScopeContextHolder.withBypass(action);
    }

    private void requireSystemOperator() {
        var user = SecUtil.getCurrentUser();
        if (user == null
                || user.getAuthorities()
                        .stream()
                        .noneMatch(authority -> "ROLE_DEV_OPS".equals(authority.getAuthority()) || "*".equals(authority.getAuthority()))) {
            throw new DataScopeViolationException("当前用户无权临时绕过数据隔离");
        }
    }
}

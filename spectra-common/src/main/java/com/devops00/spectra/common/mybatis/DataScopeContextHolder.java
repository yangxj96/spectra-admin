/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.common.mybatis;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 数据权限请求上下文。
 *
 * Web 请求由 framework 统一开启和清理；非 Web 线程不会缓存范围，避免线程池 复用时将上一个用户的范围带入下一个任务。绕过隔离只能通过
 * framework 提供 的受控执行器进入，拦截器只消费这里的状态。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
public final class DataScopeContextHolder {

    private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<>();

    private DataScopeContextHolder() {
    }

    /**
     * 开启当前线程的数据权限请求上下文。
     */
    public static void beginRequest() {
        CONTEXT.set(new Context());
    }

    /**
     * 清理当前线程的数据权限请求上下文。
     */
    public static void endRequest() {
        CONTEXT.remove();
    }

    /**
     * 判断当前线程是否处于受控的数据隔离绕过区域。
     */
    public static boolean isBypassed() {
        Context context = CONTEXT.get();
        return context != null && context.bypassDepth > 0;
    }

    /**
     * 读取当前用户在请求上下文中的有效数据范围。
     */
    public static DataScopeProvider.EffectiveScope getScope(UUID userId) {
        Context context = CONTEXT.get();
        if (context == null || !userId.equals(context.userId)) {
            return null;
        }
        return context.scope;
    }

    /**
     * 保存当前用户在请求上下文中的有效数据范围。
     */
    public static void setScope(UUID userId, DataScopeProvider.EffectiveScope scope) {
        Context context = CONTEXT.get();
        if (context != null) {
            context.userId = userId;
            context.scope = scope;
        }
    }

    /**
     * 在受控范围内执行需要绕过数据隔离的返回值任务。
     */
    public static <T> T withBypass(Supplier<T> action) {
        Context context = CONTEXT.get();
        boolean created = context == null;
        if (created) {
            context = new Context();
            CONTEXT.set(context);
        }
        context.bypassDepth++;
        try {
            return action.get();
        } finally {
            context.bypassDepth--;
            if (created) {
                CONTEXT.remove();
            }
        }
    }

    /**
     * 在受控范围内执行需要绕过数据隔离的无返回值任务。
     */
    public static void withBypass(Runnable action) {
        withBypass(() -> {
            action.run();
            return null;
        });
    }

    private static final class Context {
        private UUID userId;
        private DataScopeProvider.EffectiveScope scope;
        private int bypassDepth;
    }
}

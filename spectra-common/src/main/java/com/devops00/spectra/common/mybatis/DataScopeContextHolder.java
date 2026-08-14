/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.common.mybatis;

import java.util.function.Supplier;

/**
 * 数据权限请求上下文。
 * <p>
 * Web 请求由 framework 统一开启和清理；当前上下文只记录受控绕过深度，不再缓存旧版全局
 * EffectiveScope。绕过隔离只能通过 framework 提供的受控执行器进入，拦截器只消费这里的状态。
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
        private int bypassDepth;
    }
}

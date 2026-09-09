/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.common.mybatis;

import java.lang.ScopedValue;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * 数据权限请求或受控执行上下文。
 * <p>
 * Web 请求由 framework 通过 callback 作用域开启；当前上下文只记录受控绕过深度，不再缓存旧版全局
 * EffectiveScope。绕过隔离只能通过 framework 提供的受控执行器进入，拦截器只消费这里的状态；请求结束或
 * 受控动作返回后，ScopedValue 会自动恢复进入前的深度。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
public final class DataScopeContextHolder {

    private static final ScopedValue<Integer> BYPASS_DEPTH = ScopedValue.newInstance();

    private DataScopeContextHolder() {
    }

    /**
     * 在请求 callback 期间绑定初始绕过深度 0；callback 返回或抛出异常后自动解除绑定。
     *
     * @param action 请求期间执行的业务动作；动作抛出的受检异常原样传播
     * @param <T>    动作返回值类型
     * @return action 产生的业务结果；action 返回 null 时原样返回 null
     * @throws Exception action 抛出的受检异常
     */
    public static <T> T callWithRequest(Callable<T> action) throws Exception {
        Objects.requireNonNull(action, "action");
        return ScopedValue.where(BYPASS_DEPTH, 0).call(action::call);
    }

    /**
     * 判断当前执行作用域是否处于受控的数据隔离绕过区域。
     *
     * @return 当前执行作用域内存在至少一个绕过层级时返回 {@code true}，未绑定请求或未绕过时返回 {@code false}
     */
    public static boolean isBypassed() {
        return BYPASS_DEPTH.orElse(0) > 0;
    }

    /**
     * 在受控范围内执行需要绕过数据隔离的返回值任务；嵌套调用只增加当前词法作用域的深度。
     *
     * @param action 需要临时绕过数据隔离的受信任内部操作
     * @param <T>    操作返回值类型
     * @return action 产生的业务结果；action 返回 null 时原样返回 null
     */
    public static <T> T withBypass(Supplier<T> action) {
        Objects.requireNonNull(action, "action");
        int depth = BYPASS_DEPTH.orElse(0);
        try {
            return ScopedValue.where(BYPASS_DEPTH, depth + 1).call(action::get);
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalStateException("数据权限绕过动作执行失败", exception);
        }
    }

    /**
     * 在受控范围内执行需要绕过数据隔离的无返回值任务。
     *
     * @param action 需要临时绕过数据隔离的受信任内部操作；动作结束后自动恢复进入前的绕过状态
     */
    public static void withBypass(Runnable action) {
        Objects.requireNonNull(action, "action");
        withBypass(() -> {
            action.run();
            return null;
        });
    }
}

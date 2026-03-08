package com.devops00.spectra.kernel.lifecycle;

/// 模块生命周期
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/8 23:33
public interface SpectraModuleLifecycle {

    /// 初始化之前执行
    default void beforeInitialize() {
    }

    /// 初始化之后执行
    default void afterInitialize() {
    }

    /// 开始
    default void onStart() {
    }

}
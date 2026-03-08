package com.devops00.spectra.kernel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// 模块注解
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/8 23:33
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpectraModule {

    /**
     * 模块名称
     */
    String name();

    /**
     * 扫描包
     */
    String[] scanPackages() default {};

    /**
     * mapper包
     */
    String[] mapperPackages() default {};

    /**
     * 依赖模块
     */
    String[] dependsOn() default {};

    /**
     * 启动顺序
     */
    int order() default 0;
}
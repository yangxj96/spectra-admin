package com.devops00.spectra.kernel.model;


import lombok.Getter;
import lombok.Setter;

/// 模块描述对象
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/8 23:33
@Getter
@Setter
public class SpectraModuleDescriptor {

    /// 模块名称
    private String name;

    /// 扫描目类
    private String[] scanPackages = {};

    /// mapper目类
    private String[] mapperPackages = {};

    /// 依赖的模块
    private String[] dependsOn = {};

    /// 排序
    private int order;

    /// 模块实例
    private Object moduleInstance;

}

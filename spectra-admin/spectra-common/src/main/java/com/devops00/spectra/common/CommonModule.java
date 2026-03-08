package com.devops00.spectra.common;


import com.devops00.spectra.kernel.annotation.SpectraModule;
import com.devops00.spectra.kernel.lifecycle.SpectraModuleLifecycle;
import lombok.extern.slf4j.Slf4j;

/// 基础设施模块
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/8 23:30
@Slf4j
@SpectraModule(name = "common", scanPackages = "com.devops00.spectra.common.assembler", order = 0)
public class CommonModule implements SpectraModuleLifecycle {

}

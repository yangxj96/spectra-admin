package com.devops00.spectra.workflow;


import com.devops00.spectra.kernel.annotation.SpectraModule;
import com.devops00.spectra.kernel.lifecycle.SpectraModuleLifecycle;

/// 文件上传模块
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/8 23:41
@SpectraModule(name = "workflow", scanPackages = "com.devops00.spectra.oa", order = 2)
public class WorkflowModule implements SpectraModuleLifecycle {
}

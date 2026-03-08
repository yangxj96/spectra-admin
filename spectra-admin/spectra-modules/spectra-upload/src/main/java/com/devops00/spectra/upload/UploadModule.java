package com.devops00.spectra.upload;


import com.devops00.spectra.kernel.annotation.SpectraModule;
import com.devops00.spectra.kernel.lifecycle.SpectraModuleLifecycle;

/// 文件上传模块
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/8 23:41
@SpectraModule(
        name = "upload",
        scanPackages = "com.devops00.spectra.upload",
        mapperPackages = "com.devops00.spectra.upload.mapper",
        order = 1
)
public class UploadModule implements SpectraModuleLifecycle {
}

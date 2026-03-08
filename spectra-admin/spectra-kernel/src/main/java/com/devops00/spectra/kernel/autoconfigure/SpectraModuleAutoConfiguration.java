package com.devops00.spectra.kernel.autoconfigure;


import com.devops00.spectra.kernel.registrar.SpectraModuleRegistrar;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/// 自动配置
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/8 23:34
@AutoConfiguration
@Import(SpectraModuleRegistrar.class)
public class SpectraModuleAutoConfiguration {

}

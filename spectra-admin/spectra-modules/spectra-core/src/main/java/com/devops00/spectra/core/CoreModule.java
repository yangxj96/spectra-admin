package com.devops00.spectra.core;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;

/// 基础设施模块
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/8 23:30
@ComponentScan("com.devops00.spectra.core")
@MapperScan("com.devops00.spectra.core.mapper")
public class CoreModule {

}

package com.devops00.spectra.oa;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;

/// 文件上传模块
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/8 23:41
@ComponentScan("com.devops00.spectra.oa")
@MapperScan("com.devops00.spectra.oa.*.mapper")
public class OaModule {
}

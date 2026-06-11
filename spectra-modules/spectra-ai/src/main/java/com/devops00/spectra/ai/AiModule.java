package com.devops00.spectra.ai;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * Ai模块自动加载
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/4/26 12:12
 */
@ComponentScan("com.devops00.spectra.ai")
@MapperScan("com.devops00.spectra.ai.mapper")
public class AiModule {
}

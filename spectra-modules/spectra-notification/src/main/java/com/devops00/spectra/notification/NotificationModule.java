package com.devops00.spectra.notification;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.devops00.spectra.notification.configuration.NotificationModuleProperties;

/**
 * 统一通知模块入口。
 *
 * <p>模块只扫描自身实现，避免重新扫描 spectra-core 中的历史通知实现。
 * Launch 装配该配置后，再逐步切换公共 Gateway 的实际实现。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Configuration
@ComponentScan(basePackages = "com.devops00.spectra.notification")
@EnableConfigurationProperties(NotificationModuleProperties.class)
public class NotificationModule {
}

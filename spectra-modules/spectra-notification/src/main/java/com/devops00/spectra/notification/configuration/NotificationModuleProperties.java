package com.devops00.spectra.notification.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 通知模块基础配置。 */
@ConfigurationProperties(prefix = "spectra.notification")
public record NotificationModuleProperties(boolean enabled) {
}

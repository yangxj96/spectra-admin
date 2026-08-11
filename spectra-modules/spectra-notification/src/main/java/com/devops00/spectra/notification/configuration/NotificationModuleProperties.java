package com.devops00.spectra.notification.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** 通知模块基础配置。 */
@ConfigurationProperties(prefix = "spectra.notification")
public record NotificationModuleProperties(boolean enabled, String addressEncryptionKey, String sensitivePayloadKey) {

    /** 将未配置的可选密钥归一化为空字符串，实际使用时再明确拒绝。 */
    public NotificationModuleProperties {
        addressEncryptionKey = addressEncryptionKey == null ? "" : addressEncryptionKey.trim();
        sensitivePayloadKey = sensitivePayloadKey == null ? "" : sensitivePayloadKey.trim();
    }
}

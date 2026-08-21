/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.notification.properties;

import com.devops00.spectra.common.config.SystemConfigValueProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 通知模块基础配置。
 *
 * @param enabled              是否启用统一通知模块
 * @param addressEncryptionKey Base64 编码的收件地址加密密钥
 * @param sensitivePayloadKey  Base64 编码的敏感载荷加密密钥
 * @param allowedLinkPrefixes  允许写入消息中心的前端站内路由前缀
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@ConfigurationProperties(prefix = "spectra.notification")
public class NotificationModuleProperties {

    private static final List<String> DEFAULT_ALLOWED_LINK_PREFIXES = List.of(
            "/login", "/security/authentication/", "/oa/", "/workflow/", "/notification/", "/notification-center/", "/system/", "/file/",
            "/ai/");

    private boolean enabled = true;

    private String addressEncryptionKey = "";

    private String sensitivePayloadKey = "";

    private List<String> allowedLinkPrefixes = DEFAULT_ALLOWED_LINK_PREFIXES;

    private SystemConfigValueProvider systemConfigValueProvider;

    /**
     * 保留显式构造器，方便模块单元测试使用固定配置。
     */
    public NotificationModuleProperties(boolean enabled, String addressEncryptionKey, String sensitivePayloadKey,
                                        List<String> allowedLinkPrefixes) {
        this.enabled = enabled;
        this.addressEncryptionKey = normalize(addressEncryptionKey);
        this.sensitivePayloadKey = normalize(sensitivePayloadKey);
        this.allowedLinkPrefixes = normalizePrefixes(allowedLinkPrefixes);
    }

    /** Spring Boot 配置绑定使用的默认构造器。 */
    public NotificationModuleProperties() {
    }

    @Autowired(required = false)
    public void setSystemConfigValueProvider(SystemConfigValueProvider systemConfigValueProvider) {
        this.systemConfigValueProvider = systemConfigValueProvider;
    }

    public boolean enabled() {
        return systemValue("notification.enabled").map(Boolean::parseBoolean).orElse(enabled);
    }

    public String addressEncryptionKey() {
        return systemValue("notification.address-encryption-key").orElse(normalize(addressEncryptionKey));
    }

    public String sensitivePayloadKey() {
        return systemValue("notification.sensitive-payload-key").orElse(normalize(sensitivePayloadKey));
    }

    public List<String> allowedLinkPrefixes() {
        return systemValue("notification.allowed-link-prefixes")
                .map(value -> normalizePrefixes(Arrays.asList(value.split(","))))
                .orElseGet(() -> normalizePrefixes(allowedLinkPrefixes));
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setAddressEncryptionKey(String addressEncryptionKey) {
        this.addressEncryptionKey = addressEncryptionKey;
    }

    public void setSensitivePayloadKey(String sensitivePayloadKey) {
        this.sensitivePayloadKey = sensitivePayloadKey;
    }

    public void setAllowedLinkPrefixes(List<String> allowedLinkPrefixes) {
        this.allowedLinkPrefixes = allowedLinkPrefixes;
    }

    private Optional<String> systemValue(String key) {
        return systemConfigValueProvider == null ? Optional.empty() : systemConfigValueProvider.find(key);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static List<String> normalizePrefixes(List<String> values) {
        if (values == null || values.isEmpty()) {
            return DEFAULT_ALLOWED_LINK_PREFIXES;
        }
        var normalized = values.stream()
                .filter(item -> item != null && !item.isBlank())
                .map(String::trim)
                .filter(item -> item.startsWith("/"))
                .distinct()
                .toList();
        return normalized.isEmpty() ? DEFAULT_ALLOWED_LINK_PREFIXES : normalized;
    }
}

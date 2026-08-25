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
            "/login", "/security/authentication/", "/oa/", "/workflow/", "/notification/", "/notification-center/", "/system/", "/file/");

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

    /**
     * 更新或推进目标状态（{@code setSystemConfigValueProvider}）。
     */
    @Autowired(required = false)
    public void setSystemConfigValueProvider(SystemConfigValueProvider systemConfigValueProvider) {
        this.systemConfigValueProvider = systemConfigValueProvider;
    }

    /**
     * 处理内部业务逻辑（{@code enabled}）。
     */
    public boolean enabled() {
        return systemValue("notification.enabled").map(Boolean::parseBoolean).orElse(enabled);
    }

    /**
     * 处理内部业务逻辑（{@code addressEncryptionKey}）。
     */
    public String addressEncryptionKey() {
        return systemValue("notification.address-encryption-key").orElse(normalize(addressEncryptionKey));
    }

    /**
     * 处理内部业务逻辑（{@code sensitivePayloadKey}）。
     */
    public String sensitivePayloadKey() {
        return systemValue("notification.sensitive-payload-key").orElse(normalize(sensitivePayloadKey));
    }

    /**
     * 查询或获取目标数据（{@code allowedLinkPrefixes}）。
     */
    public List<String> allowedLinkPrefixes() {
        return systemValue("notification.allowed-link-prefixes")
                .map(value -> normalizePrefixes(Arrays.asList(value.split(","))))
                .orElseGet(() -> normalizePrefixes(allowedLinkPrefixes));
    }

    /**
     * 更新或推进目标状态（{@code setEnabled}）。
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 更新或推进目标状态（{@code setAddressEncryptionKey}）。
     */
    public void setAddressEncryptionKey(String addressEncryptionKey) {
        this.addressEncryptionKey = addressEncryptionKey;
    }

    /**
     * 更新或推进目标状态（{@code setSensitivePayloadKey}）。
     */
    public void setSensitivePayloadKey(String sensitivePayloadKey) {
        this.sensitivePayloadKey = sensitivePayloadKey;
    }

    /**
     * 更新或推进目标状态（{@code setAllowedLinkPrefixes}）。
     */
    public void setAllowedLinkPrefixes(List<String> allowedLinkPrefixes) {
        this.allowedLinkPrefixes = normalizePrefixes(allowedLinkPrefixes);
    }

    /**
     * 处理内部业务逻辑（{@code systemValue}）。
     */
    private Optional<String> systemValue(String key) {
        return systemConfigValueProvider == null ? Optional.empty() : systemConfigValueProvider.find(key);
    }

    /**
     * 转换、解析或规范化数据（{@code normalize}）。
     */
    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * 转换、解析或规范化数据（{@code normalizePrefixes}）。
     */
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

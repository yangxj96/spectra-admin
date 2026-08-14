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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.List;

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
public record NotificationModuleProperties(boolean enabled, String addressEncryptionKey, String sensitivePayloadKey,
                                           List<String> allowedLinkPrefixes) {

    private static final List<String> DEFAULT_ALLOWED_LINK_PREFIXES = List.of(
            "/login", "/auth/", "/oa/", "/workflow/", "/notification/", "/notification-center/", "/system/", "/file/",
            "/ai/");

    /**
     * 兼容现有测试和本地构造方式。
     */
    public NotificationModuleProperties(boolean enabled, String addressEncryptionKey, String sensitivePayloadKey) {
        this(enabled, addressEncryptionKey, sensitivePayloadKey, DEFAULT_ALLOWED_LINK_PREFIXES);
    }

    /**
     * 将未配置的可选密钥归一化为空字符串，实际使用时再明确拒绝。
     */
    @ConstructorBinding
    public NotificationModuleProperties {
        addressEncryptionKey = addressEncryptionKey == null ? "" : addressEncryptionKey.trim();
        sensitivePayloadKey = sensitivePayloadKey == null ? "" : sensitivePayloadKey.trim();
        allowedLinkPrefixes = allowedLinkPrefixes == null || allowedLinkPrefixes.isEmpty()
                ? DEFAULT_ALLOWED_LINK_PREFIXES
                : allowedLinkPrefixes.stream()
                        .filter(item -> item != null && !item.isBlank())
                        .map(String::trim)
                        .filter(item -> item.startsWith("/"))
                        .distinct()
                        .toList();
    }
}

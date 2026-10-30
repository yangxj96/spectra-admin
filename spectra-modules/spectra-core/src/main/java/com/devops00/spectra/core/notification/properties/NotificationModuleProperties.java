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

package com.devops00.spectra.core.notification.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 通知模块配置。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "spectra.notification")
public class NotificationModuleProperties {

    /**
     * 是否启用通知发送。
     */
    private boolean enabled = true;

    /**
     * 是否强制所有外部渠道使用模拟发送。
     */
    private boolean forceMock = false;

    /**
     * 外部 HTTP/SMTP 发送超时时间，单位毫秒。
     */
    private int timeoutMs = 5000;

    /**
     * 站内跳转链接允许的前缀；为空时不限制。
     */
    private List<String> allowedLinkPrefixes = List.of();
}

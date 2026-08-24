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

package com.devops00.spectra.notification.javabean.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Provider 配置脱敏视图。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Data
@Builder
public class NotificationProviderVO {

    /**
     * 通知渠道。
     */
    private String channel;

    /**
     * Provider 类型。
     */
    private String providerType;

    /**
     * Provider 状态。
     */
    private String state;

    /**
     * 是否启用。
     */
    private boolean enabled;

    /**
     * 脱敏状态原因。
     */
    private String reason;

    /** 非敏感端点或 SMTP 主机。 */
    private String endpoint;

    /** Provider 端口。 */
    private int port;

    /** 云服务地域。 */
    private String region;

    /** AccessKey ID、SecretId 或 SMTP 用户名。 */
    private String credentialId;

    /** 腾讯云短信 SDK AppID。 */
    private String appId;

    /** 短信签名或邮件发件人名称。 */
    private String signName;

    /** 邮件发件地址。 */
    private String senderAddress;

    /** 邮件发件人显示名称。 */
    private String senderName;

    /** 是否启用隐式 SSL。 */
    private boolean sslEnabled;

    /** 是否启用 STARTTLS。 */
    private boolean starttlsEnabled;

    /**
     * 请求超时毫秒数。
     */
    private int timeoutMs;

    /**
     * 每秒发送上限。
     */
    private int rateLimitPerSecond;

    /**
     * 最大投递尝试次数。
     */
    private int maxAttempts;

    /**
     * 外部模板编码。
     */
    private String templateCode;

    /**
     * 外部模板参数顺序。
     */
    private String templateParameterOrder;

    /**
     * 是否已配置 Secret。
     */
    private boolean secretConfigured;

    /**
     * Secret 标识；不返回密文。
     */
    private String secretKeyId;

    /**
     * 配置更新时间。
     */
    private LocalDateTime updatedAt;
}

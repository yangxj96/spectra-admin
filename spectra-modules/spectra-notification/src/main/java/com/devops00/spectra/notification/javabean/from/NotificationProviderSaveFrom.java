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

package com.devops00.spectra.notification.javabean.from;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Provider 配置保存入参；Secret 为空时表示保持原值。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Data
public class NotificationProviderSaveFrom {

    /**
     * Provider 类型；支持阿里云短信、腾讯云短信、SMTP、HTTP_JSON 和 MOCK。
     */
    @NotBlank(message = "Provider 类型不能为空")
    @Size(max = 32, message = "Provider 类型长度不能超过 32 个字符")
    private String providerType;

    /**
     * 是否启用 Provider。
     */
    private boolean enabled;

    /**
     * Provider 端点或 SMTP 主机；MOCK 类型可以为空。
     */
    @Size(max = 500, message = "Provider 端点长度不能超过 500 个字符")
    private String endpoint;

    /**
     * Provider 端口；云服务通常不需要填写，SMTP 常用 465 或 587。
     */
    @Min(value = 0, message = "Provider 端口不能小于 0")
    @Max(value = 65_535, message = "Provider 端口不能超过 65535")
    private int port;

    /**
     * 云服务地域，例如 cn-hangzhou 或 ap-guangzhou。
     */
    @Size(max = 64, message = "Provider 地域长度不能超过 64 个字符")
    private String region;

    /**
     * AccessKey ID、SecretId 或 SMTP 用户名；非敏感字段。
     */
    @Size(max = 200, message = "Provider 凭据标识长度不能超过 200 个字符")
    private String credentialId;

    /**
     * 腾讯云短信 SDK AppID。
     */
    @Size(max = 64, message = "短信 SDK AppID 长度不能超过 64 个字符")
    private String appId;

    /**
     * 已审核的短信签名或邮件发件人名称。
     */
    @Size(max = 200, message = "签名或发件人名称长度不能超过 200 个字符")
    private String signName;

    /**
     * 邮件 From 地址。
     */
    @Size(max = 320, message = "邮件发件地址长度不能超过 320 个字符")
    private String senderAddress;

    /**
     * 邮件 From 显示名称。
     */
    @Size(max = 200, message = "邮件发件人名称长度不能超过 200 个字符")
    private String senderName;

    /**
     * 是否使用隐式 SSL；SMTP 465 常用。
     */
    private boolean sslEnabled;

    /**
     * 是否使用 STARTTLS；SMTP 587 常用。
     */
    private boolean starttlsEnabled;

    /**
     * 请求超时毫秒数。
     */
    @Min(value = 100, message = "Provider 超时不能小于 100 毫秒")
    @Max(value = 30_000, message = "Provider 超时不能超过 30000 毫秒")
    private int timeoutMs = 5_000;

    /**
     * 每秒发送上限。
     */
    @Min(value = 1, message = "Provider 限流值必须大于 0")
    @Max(value = 10_000, message = "Provider 限流值不能超过 10000")
    private int rateLimitPerSecond = 10;

    /**
     * 最大投递尝试次数。
     */
    @Min(value = 1, message = "Provider 重试次数必须大于 0")
    @Max(value = 5, message = "Provider 重试次数不能超过 5 次")
    private int maxAttempts = 3;

    /**
     * 外部供应商模板编码。
     */
    @Size(max = 100, message = "供应商模板编码长度不能超过 100 个字符")
    private String templateCode;

    /**
     * 腾讯云模板参数顺序，逗号分隔；例如 code,userName。
     */
    @Size(max = 500, message = "模板参数顺序长度不能超过 500 个字符")
    private String templateParameterOrder;

    /**
     * 新 Secret；不回显旧值。
     */
    @Size(max = 2_000, message = "Provider Secret 长度不能超过 2000 个字符")
    private String secret;

    /**
     * 是否清除当前 Secret。
     */
    private boolean clearSecret;
}

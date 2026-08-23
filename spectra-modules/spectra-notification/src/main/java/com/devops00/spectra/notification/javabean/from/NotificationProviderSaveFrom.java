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
     * Provider 类型；当前契约支持 HTTP_JSON 和 MOCK。
     */
    @NotBlank(message = "Provider 类型不能为空")
    @Size(max = 32, message = "Provider 类型长度不能超过 32 个字符")
    private String providerType;

    /**
     * 是否启用 Provider。
     */
    private boolean enabled;

    /**
     * Provider HTTP 端点；MOCK 类型可以为空。
     */
    @Size(max = 500, message = "Provider 端点长度不能超过 500 个字符")
    private String endpoint;

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
     * 新 Secret；不回显旧值。
     */
    @Size(max = 2_000, message = "Provider Secret 长度不能超过 2000 个字符")
    private String secret;

    /**
     * 是否清除当前 Secret。
     */
    private boolean clearSecret;
}

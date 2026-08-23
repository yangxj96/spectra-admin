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

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationPurpose;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 受控发送 Preview 请求；只接受非敏感模板参数。
 */
@Data
public class NotificationControlledSendFrom {

    /**
     * Apply 时交给统一 Gateway 的业务幂等键。
     */
    @NotBlank(message = "发送幂等键不能为空")
    @Size(max = 200, message = "发送幂等键长度不能超过 200 个字符")
    private String idempotencyKey;

    /**
     * 通知用途。
     */
    @NotNull(message = "通知用途不能为空")
    private NotificationPurpose purpose;

    /**
     * 发送渠道。
     */
    @NotEmpty(message = "发送渠道不能为空")
    @Size(max = 3, message = "发送渠道不能超过 3 个")
    private List<NotificationChannel> channels;

    /**
     * 每个渠道锁定的已发布模板版本。
     */
    @NotEmpty(message = "模板版本不能为空")
    private Map<NotificationChannel, UUID> templateVersionIds;

    /**
     * 受众范围。
     */
    @NotNull(message = "受众不能为空")
    @Valid
    private NotificationAudienceFrom audience;

    /**
     * 非敏感模板参数。
     */
    @NotNull(message = "模板参数不能为空")
    @Size(max = 100, message = "模板参数不能超过 100 个")
    private Map<String, Object> parameters;

    /**
     * 可选业务弱引用，Apply 时统一写入通知请求。
     */
    @Size(max = 100, message = "业务类型长度不能超过 100 个字符")
    private String businessType;

    /**
     * 可选业务对象弱引用。
     */
    @Size(max = 200, message = "业务 ID 长度不能超过 200 个字符")
    private String businessId;

    /**
     * 站内跳转链接；由 Gateway 执行最终白名单校验。
     */
    @Size(max = 500, message = "跳转链接长度不能超过 500 个字符")
    private String link;
}

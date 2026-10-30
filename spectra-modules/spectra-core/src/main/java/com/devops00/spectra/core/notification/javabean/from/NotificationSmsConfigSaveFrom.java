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

package com.devops00.spectra.core.notification.javabean.from;

import com.devops00.spectra.core.notification.javabean.enums.SmsProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;

/**
 * 短信配置保存参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Data
public class NotificationSmsConfigSaveFrom {

    /**
     * 配置名称。
     */
    @NotBlank(message = "配置名称不能为空")
    @Size(max = 200, message = "配置名称长度不能超过200")
    private String name;
    /**
     * 供应商类型。
     */
    @NotNull(message = "短信供应商不能为空")
    private SmsProviderType provider;
    /**
     * 访问密钥 ID。
     */
    private String accessKeyId;
    /**
     * 访问密钥 Secret。
     */
    private String accessKeySecret;
    /**
     * 短信签名。
     */
    private String signName;
    /**
     * 默认供应商模板编码。
     */
    private String providerTemplateCode;
    /**
     * 供应商地域。
     */
    private String region;
    /**
     * 供应商扩展字段。
     */
    private Map<String, Object> extra;
}

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

package com.devops00.spectra.core.notification.javabean.vo;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 短信配置响应对象。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Data
public class NotificationSmsConfigVO {

    /**
     * 配置 ID。
     */
    private UUID id;
    /**
     * 配置名称。
     */
    private String name;
    /**
     * 供应商类型。
     */
    private String provider;
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
    /**
     * 是否当前启用配置。
     */
    private Boolean active;
    /**
     * 创建时间。
     */
    private OffsetDateTime createdAt;
}

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
import java.util.UUID;

/**
 * 通知模板响应对象。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Data
public class NotificationTemplateVO {

    /**
     * 模板 ID。
     */
    private UUID id;
    /**
     * 模板编码。
     */
    private String code;
    /**
     * 模板名称。
     */
    private String name;
    /**
     * 适用渠道。
     */
    private String channel;
    /**
     * 标题模板。
     */
    private String titleTemplate;
    /**
     * 纯文本正文模板。
     */
    private String contentTemplate;
    /**
     * HTML 正文模板。
     */
    private String htmlTemplate;
    /**
     * 供应商模板编码。
     */
    private String providerTemplateCode;
    /**
     * 是否内置模板。
     */
    private Boolean builtin;
    /**
     * 是否启用。
     */
    private Boolean enabled;
    /**
     * 创建时间。
     */
    private OffsetDateTime createdAt;
}

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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 通知模板管理视图。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/23
 */
@Data
public class NotificationTemplateVO {

    private UUID id;

    private String templateGroupCode;

    private String templateName;

    private String channel;

    private String purpose;

    private Integer versionNo;

    private String state;

    private String versionDigest;

    private String titleTemplate;

    private String contentTemplate;

    private String htmlTemplate;

    private Map<String, Object> parameterSchema;

    private String providerTemplateCode;

    private Long version;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

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

package com.devops00.spectra.notification.template.javabean.entity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.mybatis.PgJsonbTypeHandler;
import lombok.Data;

/** 通知模板实体。 */
@Data
@TableName(value = "ntf_template", schema = "spectra_notification", autoResultMap = true)
public class NotificationTemplateEntity {

    private UUID id;
    private UUID tenantId;
    private String templateGroupCode;
    private String channel;
    private String purpose;
    private Integer versionNo;
    private String titleTemplate;
    private String contentTemplate;
    private String htmlTemplate;
    @TableField(typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> parameterSchema;
    private String providerTemplateCode;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deleted;
}

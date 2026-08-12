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

package com.devops00.spectra.notification.javabean.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.common.mybatis.PgJsonbTypeHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.UUID;

/**
 * 通知模板实体。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Getter
@Setter
@ToString
@TableName(value = "ntf_template", schema = "spectra_notification", autoResultMap = true)
public class NotificationTemplateEntity extends BaseEntity {

    /**
     * 租户 ID。
     */
    @TableField("tenant_id")
    private UUID tenantId;
    /**
     * 逻辑模板组编码。
     */
    @TableField("template_group_code")
    private String templateGroupCode;
    /**
     * 适用渠道。
     */
    @TableField("channel")
    private String channel;
    /**
     * 适用通知用途。
     */
    @TableField("purpose")
    private String purpose;
    /**
     * 模板业务版本号。
     */
    @TableField("version_no")
    private Integer versionNo;
    /**
     * 标题模板。
     */
    @TableField("title_template")
    private String titleTemplate;
    /**
     * 纯文本正文模板。
     */
    @TableField("content_template")
    private String contentTemplate;
    /**
     * HTML 正文模板。
     */
    @TableField("html_template")
    private String htmlTemplate;
    /**
     * 模板参数白名单。
     */
    @TableField(value = "parameter_schema", typeHandler = PgJsonbTypeHandler.class, updateStrategy = FieldStrategy.ALWAYS)
    private Map<String, Object> parameterSchema;
    /**
     * 外部供应商模板编码。
     */
    @TableField("provider_template_code")
    private String providerTemplateCode;
    /**
     * 是否启用。
     */
    @TableField("enabled")
    private Boolean enabled;
}

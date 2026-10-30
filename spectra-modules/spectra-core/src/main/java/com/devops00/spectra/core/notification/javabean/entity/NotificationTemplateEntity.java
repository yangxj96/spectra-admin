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

package com.devops00.spectra.core.notification.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.framework.persistence.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 通知模板实体。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ntf_template", schema = "spectra_core")
public class NotificationTemplateEntity extends BaseEntity {

    /**
     * 模板编码；同一渠道内唯一。
     */
    @TableField("code")
    private String code;
    /**
     * 模板名称。
     */
    @TableField("name")
    private String name;
    /**
     * 适用渠道：IN_APP、SMS 或 EMAIL。
     */
    @TableField("channel")
    private String channel;
    /**
     * 标题模板；短信和站内信可空。
     */
    @TableField("title_template")
    private String titleTemplate;
    /**
     * 纯文本正文模板。
     */
    @TableField("content_template")
    private String contentTemplate;
    /**
     * HTML 正文模板；仅邮件使用。
     */
    @TableField("html_template")
    private String htmlTemplate;
    /**
     * 外部供应商模板编码；短信使用。
     */
    @TableField("provider_template_code")
    private String providerTemplateCode;
    /**
     * 是否内置模板。
     */
    @TableField("builtin")
    private Boolean builtin;
    /**
     * 是否启用。
     */
    @TableField("enabled")
    private Boolean enabled;
}

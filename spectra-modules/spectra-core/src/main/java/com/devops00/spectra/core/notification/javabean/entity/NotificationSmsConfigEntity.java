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

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.framework.persistence.base.BaseEntity;
import com.devops00.spectra.framework.persistence.mybatis.PgJsonbTypeHandler;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * 短信发送配置实体；同一时刻只允许一条启用配置。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ntf_sms_config", schema = "spectra_core", autoResultMap = true)
public class NotificationSmsConfigEntity extends BaseEntity {

    /**
     * 配置名称。
     */
    @TableField("name")
    private String name;
    /**
     * 供应商类型：ALIYUN、TENCENT 或 MOCK。
     */
    @TableField("provider")
    private String provider;
    /**
     * 供应商访问密钥 ID。
     */
    @TableField("access_key_id")
    private String accessKeyId;
    /**
     * 供应商访问密钥 Secret。
     */
    @TableField("access_key_secret")
    private String accessKeySecret;
    /**
     * 短信签名。
     */
    @TableField("sign_name")
    private String signName;
    /**
     * 默认供应商模板编码；模板未指定时使用。
     */
    @TableField("provider_template_code")
    private String providerTemplateCode;
    /**
     * 供应商地域。
     */
    @TableField("region")
    private String region;
    /**
     * 腾讯云短信应用 ID 等供应商扩展字段。
     */
    @TableField(value = "extra", typeHandler = PgJsonbTypeHandler.class, updateStrategy = FieldStrategy.ALWAYS)
    private Map<String, Object> extra;
    /**
     * 是否当前启用配置。
     */
    @TableField("active")
    private Boolean active;
}

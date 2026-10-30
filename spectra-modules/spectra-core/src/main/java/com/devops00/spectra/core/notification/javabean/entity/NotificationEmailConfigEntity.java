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
 * 邮件发送配置实体；同一时刻只允许一条启用配置。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/30
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
@TableName(value = "ntf_email_config", schema = "spectra_core", autoResultMap = true)
public class NotificationEmailConfigEntity extends BaseEntity {

    /**
     * 配置名称。
     */
    @TableField("name")
    private String name;
    /**
     * 供应商类型：SMTP 或 MOCK。
     */
    @TableField("provider")
    private String provider;
    /**
     * SMTP 主机。
     */
    @TableField("host")
    private String host;
    /**
     * SMTP 端口。
     */
    @TableField("port")
    private Integer port;
    /**
     * SMTP 用户名。
     */
    @TableField("username")
    private String username;
    /**
     * SMTP 密码。
     */
    @TableField("password")
    private String password;
    /**
     * 发件地址。
     */
    @TableField("from_address")
    private String fromAddress;
    /**
     * 发件人名称。
     */
    @TableField("from_name")
    private String fromName;
    /**
     * 连接加密方式：NONE、SSL 或 STARTTLS。
     */
    @TableField("ssl_mode")
    private String sslMode;
    /**
     * 供应商扩展字段。
     */
    @TableField(value = "extra", typeHandler = PgJsonbTypeHandler.class, updateStrategy = FieldStrategy.ALWAYS)
    private Map<String, Object> extra;
    /**
     * 是否当前启用配置。
     */
    @TableField("active")
    private Boolean active;
}

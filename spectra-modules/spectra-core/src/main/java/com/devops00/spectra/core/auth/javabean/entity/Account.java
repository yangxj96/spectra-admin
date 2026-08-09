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

package com.devops00.spectra.core.auth.javabean.entity;

import java.time.Instant;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.security.base.constant.LoginType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 账号表
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/11 15:43
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_account", schema = "spectra_core")
public class Account extends BaseEntity {

    /**
     * 用户 ID
     */
    @TableField(value = "user_id")
    private UUID userId;

    /**
     * 登录类型
     */
    @TableField(value = "type")
    private LoginType type;

    /**
     * 用户名（用于账号密码登录）
     */
    @TableField(value = "login_name")
    private String loginName;

    /**
     * 密码(仅用作账号密码登录)
     */
    @TableField(value = "password")
    private String password;

    /**
     * 手机号（用于短信登录）
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 邮箱（用于邮箱验证码登录）
     */
    @TableField(value = "email")
    private String email;

    /**
     * 微信 openid
     */
    @TableField(value = "openid")
    private String openId;

    /**
     * 微信 unionid（跨应用唯一）
     */
    @TableField(value = "unionid")
    private String unionid;

    /**
     * 第三方来源：WECHAT, ALIPAY, APPLE 等
     */
    @TableField(value = "provider")
    private String provider;

    /**
     * 1:正常 2:禁用 3:未验证
     */
    @TableField(value = "status")
    private Short status;

    /**
     * 0:未验证 1:已验证（如手机号/邮箱）
     */
    @TableField(value = "verified")
    private Short verified;

    /**
     * 用于临时账号（如扫码未确认）
     */
    @TableField(value = "expires_at")
    private Instant expiresAt;
}

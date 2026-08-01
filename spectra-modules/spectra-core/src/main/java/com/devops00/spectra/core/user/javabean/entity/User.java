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

package com.devops00.spectra.core.user.javabean.entity;

import java.time.Instant;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/// 用户信息
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/14 00:00
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user", schema = "spectra_core")
public class User extends BaseEntity {

    /// 显示名称
    @TableField(value = "username")
    private String username;

    /// 头像
    @TableField(value = "avatar")
    private String avatar;

    /// 状态 (1:正常 0:禁用)
    @TableField(value = "status")
    private Short status;

    /// 真实姓名
    @TableField(value = "real_name")
    private String realName;

    /// 性别(0:保密,1-男,2-女)
    @TableField(value = "gender")
    private Short gender;

    /// 生日
    @TableField(value = "birthday")
    private Instant birthday;

    /// 手机号
    @TableField(value = "phone")
    private String phone;

    /// 邮箱
    @TableField(value = "email")
    private String email;

    /// 国家
    @TableField(value = "country")
    private String country;

    /// 城市
    @TableField(value = "city")
    private String city;

    /// 语言
    @TableField(value = "\"language\"")
    private String language;

    /// 时区
    @TableField(value = "timezone")
    private String timezone;

    /// 组织机构ID
    @TableField(value = "department_id")
    private UUID departmentId;
}

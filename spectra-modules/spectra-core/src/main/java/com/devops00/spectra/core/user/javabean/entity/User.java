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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * 用户信息
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user", schema = "spectra_core")
public class User extends BaseEntity {

    /**
     * 工号/员工编号。
     */
    @TableField(value = "employee_no")
    private String employeeNo;

    /**
     * 头像
     */
    @TableField(value = "avatar")
    private String avatar;

    /**
     * 生命周期状态：ACTIVE、LOCKED、DISABLED、DEPARTED。
     */
    @TableField(value = "status")
    private UserStatus status;

    /**
     * 最近一次生命周期变更原因。
     */
    @TableField(value = "status_reason")
    private String statusReason;

    /**
     * 离职时间。
     */
    @TableField(value = "departed_at")
    private Instant departedAt;

    /**
     * 真实姓名
     */
    @TableField(value = "real_name")
    private String realName;

    /**
     * 手机号
     */
    @TableField(value = "phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 语言
     */
    @TableField(value = "\"language\"")
    private String language;

    /**
     * 时区
     */
    @TableField(value = "timezone")
    private String timezone;

    /**
     * 主部门 ID；用户的完整组织关系由 sys_user_department_membership 保存。
     */
    @TableField(value = "primary_department_id")
    private UUID departmentId;

    /**
     * 安全相关变化版本；每次生命周期变化递增，用于 Session/Authorization epoch 校验。
     */
    @TableField(value = "security_version")
    private Long securityVersion;
}

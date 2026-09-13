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

package com.devops00.spectra.core.security.authentication.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.framework.persistence.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * 用户认证与通知联系方式。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_user_contact", schema = "spectra_security")
public class UserContact extends BaseEntity {

    /**
     * 该联系方式所属的用户 ID。
     */
    @TableField(value = "user_id")
    private UUID userId;

    /**
     * 联系方式类型编码，例如邮箱或手机号。
     */
    @TableField(value = "contact_type")
    private String contactType;

    /**
     * 联系方式的规范化值。
     */
    @TableField(value = "contact_value")
    private String contactValue;

    /**
     * 联系方式当前的验证或启用状态。
     */
    @TableField(value = "state")
    private String state;

    /**
     * 该联系方式最近一次通过验证的时间。
     */
    @TableField(value = "verified_at")
    private Instant verifiedAt;
}

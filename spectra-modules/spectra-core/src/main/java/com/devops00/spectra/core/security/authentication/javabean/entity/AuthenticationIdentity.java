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
 * 认证身份标识。原始登录标识不落库，只保存规范化标识的摘要。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_authentication_identity", schema = "spectra_security")
public class AuthenticationIdentity extends BaseEntity {

    /**
     * 该认证身份所属的用户 ID。
     */
    @TableField(value = "user_id")
    private UUID userId;

    /**
     * 此身份使用的认证方式编码。
     */
    @TableField(value = "method_code")
    private String methodCode;

    /**
     * 提供此认证身份的认证源或身份提供方编码。
     */
    @TableField(value = "provider_code")
    private String providerCode;

    /**
     * 规范化登录标识的摘要，不保存原始登录标识。
     */
    @TableField(value = "identifier_hash")
    private String identifierHash;

    /**
     * 认证身份当前的启用或停用状态。
     */
    @TableField(value = "state")
    private String state;

    /**
     * 该身份最近一次通过验证的时间。
     */
    @TableField(value = "verified_at")
    private Instant verifiedAt;

    /**
     * 该身份最近一次用于认证的时间。
     */
    @TableField(value = "last_used_at")
    private Instant lastUsedAt;

}

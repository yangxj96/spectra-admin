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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * 认证身份标识。原始登录标识不落库，只保存规范化标识的摘要。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "authentication_identity", schema = "spectra_security")
public class AuthenticationIdentity {

    @TableId(value = "id", type = IdType.INPUT)
    private UUID id;

    @TableField(value = "user_id")
    private UUID userId;

    @TableField(value = "method_code")
    private String methodCode;

    @TableField(value = "provider_code")
    private String providerCode;

    @TableField(value = "identifier_hash")
    private String identifierHash;

    @TableField(value = "state")
    private String state;

    @TableField(value = "verified_at")
    private Instant verifiedAt;

    @TableField(value = "last_used_at")
    private Instant lastUsedAt;

    @TableField(value = "created_at")
    private Instant createdAt;

    @TableField(value = "updated_at")
    private Instant updatedAt;

    @TableField(value = "version")
    private Long version;
}

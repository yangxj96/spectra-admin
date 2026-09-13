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
 * 密码凭证。密码明文永远不进入实体或审计快照。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_password_credential", schema = "spectra_security")
public class PasswordCredential extends BaseEntity {

    /**
     * 该密码凭证所属的用户 ID。
     */
    @TableField(value = "user_id")
    private UUID userId;

    /**
     * 用户密码的单向哈希值，不保存密码明文。
     */
    @TableField(value = "password_hash")
    private String passwordHash;

    /**
     * 密码最近一次设置或修改的时间。
     */
    @TableField(value = "changed_at")
    private Instant changedAt;

    /**
     * 当前密码凭证失效的时间；为空时不设置到期时间。
     */
    @TableField(value = "expires_at")
    private Instant expiresAt;

    /**
     * 是否要求用户在下次登录时修改密码。
     */
    @TableField(value = "must_change")
    private Boolean mustChange;

    /**
     * 当前连续密码验证失败次数。
     */
    @TableField(value = "failed_attempts")
    private Integer failedAttempts;

    /**
     * 密码凭证锁定状态的截止时间；为空时未锁定。
     */
    @TableField(value = "locked_until")
    private Instant lockedUntil;

    /**
     * 密码凭证的乐观锁版本号。
     */
    @TableField(value = "version")
    private Long version;
}

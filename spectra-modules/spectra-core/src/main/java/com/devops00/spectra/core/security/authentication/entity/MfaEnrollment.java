/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** MFA 因子登记聚合。 */
@Data
@NoArgsConstructor
@TableName(value = "mfa_enrollment", schema = "spectra_security")
public class MfaEnrollment {

    @TableId(value = "id", type = IdType.INPUT)
    private UUID id;

    @TableField("user_id")
    private UUID userId;

    @TableField("factor_type")
    private String factorType;

    @TableField("state")
    private String state;

    @TableField("enrolled_at")
    private Instant enrolledAt;

    @TableField("revoked_at")
    private Instant revokedAt;

    @TableField("created_at")
    private Instant createdAt;

    @TableField("version")
    private Long version;
}

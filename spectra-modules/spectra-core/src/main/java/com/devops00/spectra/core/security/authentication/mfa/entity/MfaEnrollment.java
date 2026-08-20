/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication.mfa.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** MFA 因子登记聚合。 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_mfa_enrollment", schema = "spectra_security")
public class MfaEnrollment extends BaseEntity {

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

}

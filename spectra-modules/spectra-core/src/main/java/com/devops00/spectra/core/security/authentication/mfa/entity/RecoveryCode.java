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

/** 单次消费 Recovery Code；只保存 PBKDF2 摘要。 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_recovery_code", schema = "spectra_security")
public class RecoveryCode extends BaseEntity {

    @TableField("enrollment_id")
    private UUID enrollmentId;

    @TableField("code_hash")
    private String codeHash;

    @TableField("used_at")
    private Instant usedAt;

}

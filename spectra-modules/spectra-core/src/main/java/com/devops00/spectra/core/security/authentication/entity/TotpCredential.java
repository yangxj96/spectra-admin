/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** 加密后的 TOTP 密钥；明文只存在于登记/校验的短生命周期内。 */
@Data
@NoArgsConstructor
@TableName(value = "totp_credential", schema = "spectra_security")
public class TotpCredential {

    @TableId(value = "enrollment_id")
    private UUID enrollmentId;

    @TableField("encrypted_secret")
    private byte[] encryptedSecret;

    @TableField("key_version")
    private String keyVersion;

    @TableField("created_at")
    private Instant createdAt;
}

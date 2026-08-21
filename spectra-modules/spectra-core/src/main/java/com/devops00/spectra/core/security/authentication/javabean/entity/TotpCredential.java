/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/** 加密后的 TOTP 密钥；明文只存在于登记/校验的短生命周期内。 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_totp_credential", schema = "spectra_security")
public class TotpCredential extends BaseEntity {

    @TableField("enrollment_id")
    private UUID enrollmentId;

    @TableField("encrypted_secret")
    private byte[] encryptedSecret;

    @TableField("key_version")
    private String keyVersion;

}

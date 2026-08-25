/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.authentication.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** 用户认证与通知联系方式。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_user_contact", schema = "spectra_security")
public class UserContact extends BaseEntity {

    @TableField(value = "user_id")
    private UUID userId;

    @TableField(value = "contact_type")
    private String contactType;

    @TableField(value = "contact_value")
    private String contactValue;

    @TableField(value = "state")
    private String state;

    @TableField(value = "verified_at")
    private Instant verifiedAt;
}

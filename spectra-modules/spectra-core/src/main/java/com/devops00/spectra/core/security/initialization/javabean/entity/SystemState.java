/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.initialization.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;

/** 系统首次初始化单例状态。 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_system_state", schema = "spectra_core")
public class SystemState extends BaseEntity {

    @TableField("state_key")
    private String stateKey;

    @TableField("state")
    private String state;

    @TableField("initialization_id")
    private UUID initializationId;

    @TableField("initialized_at")
    private Instant initializedAt;

    @TableField("initialized_by")
    private UUID initializedBy;
}

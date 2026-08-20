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

/**
 * 系统初始化与系统设置引导状态。
 * <p>
 * 每个 {@code stateKey} 只保留一条状态记录，状态值由 {@code stateKey} 对应的状态机解释。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_system_state", schema = "spectra_core")
public class SystemState extends BaseEntity {

    /**
     * 状态机键：{@code SYSTEM} 表示首次系统初始化，{@code SYSTEM_GUIDE} 表示系统设置引导。
     */
    @TableField("state_key")
    private String stateKey;

    /**
     * 状态值：{@code SYSTEM} 使用 {@code UNINITIALIZED/INITIALIZING/INITIALIZED}；
     * {@code SYSTEM_GUIDE} 使用 {@code PENDING/COMPLETED}。
     */
    @TableField("state")
    private String state;

    /**
     * 首次系统初始化流程 ID；仅 {@code SYSTEM} 状态机使用。
     */
    @TableField("initialization_id")
    private UUID initializationId;

    /**
     * 首次系统初始化完成时间；仅 {@code SYSTEM} 状态机使用。
     */
    @TableField("initialized_at")
    private Instant initializedAt;

    /**
     * 完成首次系统初始化的用户 ID；仅 {@code SYSTEM} 状态机使用。
     */
    @TableField("initialized_by")
    private UUID initializedBy;
}

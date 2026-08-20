/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.authorization.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * 目标授权模型中的角色-菜单导航关系。菜单可见性与 Permission 保持独立。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_role_menu", schema = "spectra_security")
public class SecurityRoleMenu extends BaseEntity {

    @TableField(value = "role_id")
    private UUID roleId;

    @TableField(value = "menu_id")
    private UUID menuId;
}

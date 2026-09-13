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

package com.devops00.spectra.core.security.authorization.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.framework.persistence.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * 目标授权模型中的角色与菜单导航关系；菜单可见性独立于权限授权。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_role_menu", schema = "spectra_security")
public class SecurityRoleMenu extends BaseEntity {

    /**
     * 具有该菜单导航项可见权限的安全角色 ID。
     */
    @TableField(value = "role_id")
    private UUID roleId;

    /**
     * 与安全角色关联的菜单 ID。
     */
    @TableField(value = "menu_id")
    private UUID menuId;
}

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

/**
 * 安全角色定义，包含角色类别、授权等级和系统管理属性。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_role", schema = "spectra_security")
public class SecurityRole extends BaseEntity {

    /**
     * 安全角色的唯一业务编码。
     */
    @TableField(value = "code")
    private String code;

    /**
     * 安全角色的显示名称。
     */
    @TableField(value = "name")
    private String name;

    /**
     * 安全角色当前的启用状态。
     */
    @TableField(value = "state")
    private String state;

    /**
     * 安全角色的类别编码。
     */
    @TableField(value = "role_kind")
    private String roleKind;

    /**
     * 用于比较角色授权层级的等级值。
     */
    @TableField(value = "authority_level")
    private Integer authorityLevel;

    /**
     * 是否为系统维护的内置角色。
     */
    @TableField(value = "system_managed")
    private Boolean systemManaged;

    /**
     * 安全角色的补充说明。
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 安全角色记录的乐观锁版本号。
     */
    @TableField(value = "version")
    private Long version;
}

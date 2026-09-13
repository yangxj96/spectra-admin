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
 * 系统权限目录项，定义资源、操作及可用的授权范围模式。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_permission", schema = "spectra_security")
public class Permission extends BaseEntity {

    /**
     * 权限的唯一业务编码。
     */
    @TableField(value = "code")
    private String code;

    /**
     * 权限的显示名称。
     */
    @TableField(value = "name")
    private String name;

    /**
     * 权限所属资源的业务编码。
     */
    @TableField(value = "resource_code")
    private String resourceCode;

    /**
     * 权限所代表操作的业务编码。
     */
    @TableField(value = "action_code")
    private String actionCode;

    /**
     * 该权限允许使用的授权范围模式编码集合。
     */
    @TableField(value = "allowed_scope_modes")
    private String allowedScopeModes;

    /**
     * 权限当前的启用状态。
     */
    @TableField(value = "state")
    private String state;

    /**
     * 是否为系统维护的内置权限。
     */
    @TableField(value = "system_managed")
    private Boolean systemManaged;

    /**
     * 权限记录的乐观锁版本号。
     */
    @TableField(value = "version")
    private Long version;
}

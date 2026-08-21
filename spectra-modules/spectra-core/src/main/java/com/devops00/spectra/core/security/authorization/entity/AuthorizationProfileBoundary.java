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

package com.devops00.spectra.core.security.authorization.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.common.mybatis.PgJsonbTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.UUID;

/**
 * 授权方案中的 Permission-specific Access/Grant Boundary。
 * <p>
 * JSONB 只保存模式、资源和部门业务编码，不保存运行时 Scope 或 Permission UUID。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_authorization_profile_boundary", schema = "spectra_security", autoResultMap = true)
public class AuthorizationProfileBoundary extends BaseEntity {

    @TableField(value = "profile_assignment_id")
    private UUID profileAssignmentId;

    @TableField(value = "permission_code")
    private String permissionCode;

    @TableField(value = "access_scope", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> accessScope;

    @TableField(value = "grant_scope", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> grantScope;
}

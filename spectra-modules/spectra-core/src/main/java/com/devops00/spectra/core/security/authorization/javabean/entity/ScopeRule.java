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
import com.devops00.spectra.framework.persistence.mybatis.PgJsonbTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;
import java.util.Map;

/**
 * 授权范围规则，记录规则类型、适用部门及规则专属参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_scope_rule", schema = "spectra_security")
public class ScopeRule extends BaseEntity {

    /**
     * 该规则所属的授权范围 ID。
     */
    @TableField(value = "scope_id")
    private UUID scopeId;

    /**
     * 授权范围规则的类型编码。
     */
    @TableField(value = "rule_type")
    private String ruleType;

    /**
     * 规则适用的部门 ID；规则不限定部门时可为空。
     */
    @TableField(value = "department_id")
    private UUID departmentId;

    /**
     * 规则适用范围是否包含该部门的所有后代部门。
     */
    @TableField(value = "include_descendants")
    private Boolean includeDescendants;

    /**
     * 规则类型专属的附加参数，以 JSONB 格式保存。
     */
    @TableField(value = "rule_payload", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> rulePayload;
}

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

import java.util.UUID;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_scope_rule", schema = "spectra_security")
public class ScopeRule extends BaseEntity {

    @TableField(value = "scope_id")
    private UUID scopeId;

    @TableField(value = "rule_type")
    private String ruleType;

    @TableField(value = "department_id")
    private UUID departmentId;

    @TableField(value = "include_descendants")
    private Boolean includeDescendants;

    @TableField(value = "rule_payload", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> rulePayload;
}

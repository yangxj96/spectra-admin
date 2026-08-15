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

package com.devops00.spectra.core.authorization.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@TableName(value = "sec_role_assignment", schema = "spectra_security")
public class RoleAssignment {

    @TableId(value = "id", type = IdType.INPUT)
    private UUID id;

    @TableField(value = "user_id")
    private UUID userId;

    @TableField(value = "role_id")
    private UUID roleId;

    @TableField(value = "state")
    private String state;

    @TableField(value = "valid_from")
    private Instant validFrom;

    @TableField(value = "valid_until")
    private Instant validUntil;

    @TableField(value = "version")
    private Long version;
}

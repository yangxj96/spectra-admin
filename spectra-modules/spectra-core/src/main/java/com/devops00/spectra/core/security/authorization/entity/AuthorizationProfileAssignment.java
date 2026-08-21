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
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * 授权方案中的 Role 配置快照。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_authorization_profile_assignment", schema = "spectra_security")
public class AuthorizationProfileAssignment extends BaseEntity {

    @TableField(value = "profile_id")
    private UUID profileId;

    @TableField(value = "role_code")
    private String roleCode;

    @TableField(value = "role_version")
    private Long roleVersion;
}

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
 * 可复用的授权方案元数据。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_authorization_profile", schema = "spectra_security")
public class AuthorizationProfile extends BaseEntity {

    /**
     * 授权方案的唯一业务编码。
     */
    @TableField(value = "code")
    private String code;

    /**
     * 授权方案的显示名称。
     */
    @TableField(value = "name")
    private String name;

    /**
     * 授权方案用途和适用范围的说明。
     */
    @TableField(value = "description")
    private String description;

    /**
     * 授权方案当前的启用状态。
     */
    @TableField(value = "state")
    private String state;
}

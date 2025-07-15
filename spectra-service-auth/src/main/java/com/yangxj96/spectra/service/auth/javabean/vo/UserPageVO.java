/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.service.auth.javabean.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;


/**
 * 用户分页的VO
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Data
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class UserPageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 用户状态
     */
    private Short state;

    /**
     * 角色列表
     */
    private ArrayList<RoleVO> roles;

    /**
     * 组织机构ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long organizationId;

    /**
     * 组织机构名称
     */
    private String organizationName;
}

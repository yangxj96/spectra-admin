/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.javabean.user.vo;

import io.github.yangxj96.spectra.core.configure.datascope.DataScopeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;


/// 用户分页的VO
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-6-14
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UserPageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /// 姓名
    private String username;

    /// 头像
    private String avatar;

    /// 用户状态
    private Short status;

    /// 真实姓名
    private String realName;

    /// 性别
    private Integer gender;

    /// 生日
    private LocalDate birthday;

    /// 手机号
    private String phone;

    /// 邮箱
    private String email;

    /// 国家
    private String country;

    /// 城市
    private String city;

    /// 语言
    private String language;

    /// 时区
    private String timezone;

    /// 角色列表
    private List<RoleVO> roles;

    /// 组织机构ID
    @JsonSerialize(using = ToStringSerializer.class)
    private Long organizationId;

    /// 组织机构名称
    private String organizationName;

    /// 数据范围
    private DataScopeType dataScope;

    /// 自定义时的目标ID列表
    private List<String> targetIds;
}

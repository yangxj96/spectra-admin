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

package com.devops00.spectra.core.user.javabean.from;

import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.common.constant.DataScopeType;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * 用户新增/编辑操作入参
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/16 00:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSaveFrom {

    /**
     * 主键ID
     */
    @Null(message = "新增用户时不能存在 ID", groups = Verify.Insert.class)
    @NotNull(message = "用户 ID 不能为空", groups = Verify.Update.class)
    private UUID id;

    /**
     * 姓名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 用户状态
     */
    @NotNull(message = "用户状态不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private UserStatus status;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 生日
     */
    private String birthday;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确", groups = {Verify.Insert.class, Verify.Update.class})
    @NotNull(message = "邮箱默认为登录账户,不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private String email;

    /**
     * 国家
     */
    private String country;

    /**
     * 城市
     */
    private String city;

    /**
     * 语言
     */
    private String language;

    /**
     * 时区
     */
    private String timezone;

    /**
     * 所属组织机构ID
     */
    @NotNull(message = "所属组织不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private UUID departmentId;

    /**
     * 角色ID列表
     */
    @Size(message = "角色ID列表不能为空,最少需要有一个角色", min = 1, groups = {Verify.Insert.class, Verify.Update.class})
    private List<UUID> roleIds;

    /**
     * 数据范围
     */
    private DataScopeType dataScope;

    /**
     * 当数据范围是 {@code DataScopeType#CUSTOM} 的时候这个不能为空
     */
    private List<UUID> targetIds;
}

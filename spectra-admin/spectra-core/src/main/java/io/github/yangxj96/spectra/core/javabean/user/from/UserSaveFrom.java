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

package io.github.yangxj96.spectra.core.javabean.user.from;

import io.github.yangxj96.spectra.common.base.Verify;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 用户新增/编辑操作入参
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSaveFrom {

    /**
     * 姓名
     */
    @Null(message = "新增用户时不能存在 ID", groups = Verify.Insert.class)
    @NotNull(message = "用户 ID 不能为空", groups = Verify.Update.class)
    private Long id;

    /**
     * 姓名
     */
    private String username;

    /**
     * 真实姓名
     */
    private Long realName;

    /**
     * 用户状态
     */
    @NotNull(message = "用户状态不能为空", groups = {Verify.Insert.class, Verify.Update.class})
    private Boolean status;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDateTime birthday;

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
    private Long organizationId;

    /**
     * 角色ID列表
     */
    @Size(message = "角色ID列表不能为空,最少需要有一个角色", min = 1, groups = {Verify.Insert.class, Verify.Update.class})
    private List<Long> roleIds;
}

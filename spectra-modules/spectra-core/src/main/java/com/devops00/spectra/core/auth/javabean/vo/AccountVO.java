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

package com.devops00.spectra.core.auth.javabean.vo;

import com.devops00.spectra.security.base.constant.LoginType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/// 账号绑定响应VO
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/19
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /// 账号ID
    private UUID id;

    /// 登录类型
    private LoginType type;

    /// 登录名称（用户名/手机号/邮箱）
    private String loginName;

    /// 状态（1:正常 2:禁用 3:未验证）
    private Short status;

    /// 是否已验证（0:未验证 1:已验证）
    private Short verified;

    /// 是否为当前登录方式
    private Boolean current;
}

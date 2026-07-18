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

package com.devops00.spectra.core.auth.service;


import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.core.auth.javabean.entity.Account;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/// 账号服务
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/12/11 17:05
@NullMarked
public interface AccountService extends BaseService<Account> {

    /// 根据 LoginName 字段查询账号信息
    ///
    /// @param loginName 登录用户名
    /// @return 账号信息，可能为null
    @Nullable Account getByLoginName(String loginName);

    /// 根据用户ID获取用户的默认账号
    ///
    /// @param userId 用户ID
    /// @return 账号信息
    Account getDefaultByUserId(UUID userId);

    /// 根据手机号查询账号
    ///
    /// @param phone 手机号
    /// @return 账号信息，可能为null
    @Nullable Account getByPhone(String phone);

    /// 根据邮箱查询账号
    ///
    /// @param email 邮箱
    /// @return 账号信息，可能为null
    @Nullable Account getByEmail(String email);

    /// 根据用户ID删除用户的所有登录方式
    ///
    /// @param userId 用户ID
    void deleteByUserId(UUID userId);

    /// 获取用户的所有绑定账号
    ///
    /// @param userId 用户ID
    /// @return 账号列表
    List<Account> listByUserId(UUID userId);

    /// 绑定手机号
    ///
    /// @param userId 用户ID
    /// @param phone 手机号
    /// @param code 验证码
    void bindPhone(UUID userId, String phone, String code);

    /// 绑定邮箱
    ///
    /// @param userId 用户ID
    /// @param email 邮箱
    /// @param code 验证码
    void bindEmail(UUID userId, String email, String code);

    /// 解绑账号
    ///
    /// @param userId 用户ID
    /// @param accountId 账号ID
    void unbind(UUID userId, UUID accountId);
}

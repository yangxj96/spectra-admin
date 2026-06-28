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

package com.devops00.spectra.core.auth.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.core.auth.javabean.entity.Account;
import com.devops00.spectra.core.auth.mapper.AccountMapper;
import com.devops00.spectra.core.auth.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.UUID;

/// 账号服务默认实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/12/11 17:05
@Slf4j
@Service
@NullMarked
public class AccountServiceImpl extends BaseServiceImpl<AccountMapper, Account> implements AccountService {

    @Override
    public @Nullable Account getByLoginName(String loginName) {
        var wrapper = new LambdaQueryWrapper<Account>()
                .eq(Account::getLoginName, loginName)
                .last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public Account getDefaultByUserId(UUID userId) {
        var wrapper = new LambdaQueryWrapper<Account>()
                .eq(Account::getUserId, userId)
                .isNotNull(Account::getLoginName)
                .last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public @Nullable Account getByPhone(String phone) {
        var wrapper = new LambdaQueryWrapper<Account>()
                .eq(Account::getPhone, phone)
                .last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public @Nullable Account getByEmail(String email) {
        var wrapper = new LambdaQueryWrapper<Account>()
                .eq(Account::getEmail, email)
                .last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        var wrapper = new LambdaQueryWrapper<Account>()
                .eq(Account::getUserId, userId);
        this.remove(wrapper);
    }
}

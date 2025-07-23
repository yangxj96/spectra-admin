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

package com.yangxj96.spectra.core.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yangxj96.spectra.common.base.BaseServiceImpl;
import com.yangxj96.spectra.common.enums.AccountType;
import com.yangxj96.spectra.core.auth.javabean.entity.Account;
import com.yangxj96.spectra.core.auth.mapper.AccountMapper;
import com.yangxj96.spectra.core.auth.service.AccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 账号service层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Service
public class AccountServiceImpl extends BaseServiceImpl<AccountMapper, Account> implements AccountService {

    @Override
    public Account getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getUsername, username));
    }

    @Override
    public Account getDefaultAccountByUserId(Long uid) {
        var wrapper = new LambdaQueryWrapper<Account>()
                .eq(Account::getUserId, uid)
                .eq(Account::getType, AccountType.DEFAULT)
                .last("LIMIT 1");
        return getOne(wrapper);
    }

    @Override
    @Transactional
    public void removeByUserId(Long uid) {
        var wrapper = new LambdaQueryWrapper<Account>()
                .eq(Account::getUserId, uid);
        this.remove(wrapper);
    }

    @Override
    public List<Account> getByUserId(Long uid) {
        return this.list(new LambdaQueryWrapper<Account>().eq(Account::getUserId, uid));
    }
}

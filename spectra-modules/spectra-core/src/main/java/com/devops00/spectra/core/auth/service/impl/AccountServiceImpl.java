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
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.exception.EntityUpdateException;
import com.devops00.spectra.common.exception.SpectraException;
import com.devops00.spectra.core.auth.javabean.constant.AccountStatus;
import com.devops00.spectra.core.auth.javabean.entity.Account;
import com.devops00.spectra.core.auth.mapper.AccountMapper;
import com.devops00.spectra.core.auth.service.AccountService;
import com.devops00.spectra.security.base.constant.LoginType;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 账号服务默认实现
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/11 17:05
 */
@Slf4j
@Service
@NullMarked
public class AccountServiceImpl extends BaseServiceImpl<AccountMapper, Account> implements AccountService {

    @Override
    public @Nullable Account getByLoginName(String loginName) {
        var wrapper = new LambdaQueryWrapper<Account>().eq(Account::getLoginName, loginName).last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public Account getDefaultByUserId(UUID userId) {
        var wrapper = new LambdaQueryWrapper<Account>().eq(Account::getUserId, userId).isNotNull(Account::getLoginName).last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public @Nullable Account getByPhone(String phone) {
        var wrapper = new LambdaQueryWrapper<Account>().eq(Account::getPhone, phone).last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public @Nullable Account getByEmail(String email) {
        var wrapper = new LambdaQueryWrapper<Account>().eq(Account::getEmail, email).last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        var wrapper = new LambdaQueryWrapper<Account>().eq(Account::getUserId, userId);
        this.remove(wrapper);
    }

    @Override
    public List<Account> listByUserId(UUID userId) {
        var wrapper = new LambdaQueryWrapper<Account>().eq(Account::getUserId, userId).orderByAsc(Account::getType);
        return this.list(wrapper);
    }

    @Override
    @Transactional
    public void bindPhone(UUID userId, String phone, String code) {
        // 1. 验证手机号是否已被其他用户绑定
        var existingAccount = this.getByPhone(phone);
        if (existingAccount != null && !existingAccount.getUserId().equals(userId)) {
            throw new SpectraException("该手机号已被其他账号绑定");
        }

        // 2. 如果当前用户已绑定该手机号，直接返回
        if (existingAccount != null && existingAccount.getUserId().equals(userId)) {
            return;
        }

        // 3. 创建新的 Account 记录
        var account = new Account();
        account.setUserId(userId);
        account.setType(LoginType.SMS);
        account.setPhone(phone);
        account.setProvider("DEFAULT");
        account.setStatus(AccountStatus.ACTIVE.getCode());
        account.setVerified((short) 1);
        if (!this.save(account)) {
            throw new DataSaveException("绑定手机号失败");
        }
        log.info("用户 {} 绑定手机号 {} 成功", userId, phone);
    }

    @Override
    @Transactional
    public void bindEmail(UUID userId, String email, String code) {
        // 1. 验证邮箱是否已被其他用户绑定
        var existingAccount = this.getByEmail(email);
        if (existingAccount != null && !existingAccount.getUserId().equals(userId)) {
            throw new SpectraException("该邮箱已被其他账号绑定");
        }

        // 2. 如果当前用户已绑定该邮箱，直接返回
        if (existingAccount != null && existingAccount.getUserId().equals(userId)) {
            return;
        }

        // 3. 创建新的 Account 记录
        var account = new Account();
        account.setUserId(userId);
        account.setType(LoginType.EMAIL);
        account.setEmail(email);
        account.setProvider("DEFAULT");
        account.setStatus(AccountStatus.ACTIVE.getCode());
        account.setVerified((short) 1);
        if (!this.save(account)) {
            throw new DataSaveException("绑定邮箱失败");
        }
        log.info("用户 {} 绑定邮箱 {} 成功", userId, email);
    }

    @Override
    @Transactional
    public void unbind(UUID userId, UUID accountId) {
        // 1. 查询账号是否存在
        var account = this.getById(accountId);
        if (account == null) {
            throw new DataNotExistException("账号不存在");
        }

        // 2. 校验是否是当前用户的账号
        if (!account.getUserId().equals(userId)) {
            throw new SpectraException("无权操作此账号");
        }

        // 3. 校验是否是密码登录方式（不允许解绑密码登录）
        if (account.getType() == LoginType.PASSWORD) {
            throw new SpectraException("密码登录方式不允许解绑");
        }

        // 4. 校验是否至少保留一个登录方式
        var userAccounts = this.listByUserId(userId);
        if (userAccounts.size() <= 1) {
            throw new SpectraException("至少需要保留一个登录方式");
        }

        // 5. 执行删除
        if (!this.removeById(accountId)) {
            throw new EntityUpdateException("解绑账号失败");
        }
        log.info("用户 {} 解绑账号 {} 成功", userId, accountId);
    }
}

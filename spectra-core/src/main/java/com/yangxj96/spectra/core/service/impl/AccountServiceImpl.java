package com.yangxj96.spectra.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yangxj96.spectra.common.base.BaseServiceImpl;
import com.yangxj96.spectra.common.enums.AccountType;
import com.yangxj96.spectra.core.javabean.entity.Account;
import com.yangxj96.spectra.core.javabean.entity.User;
import com.yangxj96.spectra.core.mapper.AccountMapper;
import com.yangxj96.spectra.core.service.AccountService;
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

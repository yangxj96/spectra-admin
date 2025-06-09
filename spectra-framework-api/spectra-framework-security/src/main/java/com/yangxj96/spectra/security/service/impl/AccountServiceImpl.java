package com.yangxj96.spectra.security.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yangxj96.spectra.core.base.BaseServiceImpl;
import com.yangxj96.spectra.security.entity.dto.Account;
import com.yangxj96.spectra.security.mapper.AccountMapper;
import com.yangxj96.spectra.security.service.AccountService;
import org.springframework.stereotype.Service;

@Service
public class AccountServiceImpl extends BaseServiceImpl<AccountMapper, Account> implements AccountService {

    @Override
    public Account getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getUsername, username));
    }
}

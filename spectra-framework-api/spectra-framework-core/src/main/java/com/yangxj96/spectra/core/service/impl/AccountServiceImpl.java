package com.yangxj96.spectra.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yangxj96.spectra.common.base.BaseServiceImpl;
import com.yangxj96.spectra.core.javabean.entity.Account;
import com.yangxj96.spectra.core.mapper.AccountMapper;
import com.yangxj96.spectra.core.service.AccountService;
import org.springframework.stereotype.Service;

/**
 * 账号service层-实现
 *
 * @author Jack Young
 * @since 2025/6/13 15:14
 */
@Service
public class AccountServiceImpl extends BaseServiceImpl<AccountMapper, Account> implements AccountService {

    @Override
    public Account getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<Account>()
                .eq(Account::getUsername, username));
    }
}

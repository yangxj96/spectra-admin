package io.github.yangxj96.spectra.core.service.auth.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.yangxj96.spectra.common.base.BaseServiceImpl;
import io.github.yangxj96.spectra.core.javabean.auth.entity.Account;
import io.github.yangxj96.spectra.core.mapper.auth.AccountMapper;
import io.github.yangxj96.spectra.core.service.auth.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

/// 账号服务默认实现
///
/// @author Jack Young
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
    public Account getDefaultByUserId(String userId) {
        var wrapper = new LambdaQueryWrapper<Account>()
                .eq(Account::getUserId, userId)
                .isNotNull(Account::getLoginName)
                .last("LIMIT 1");
        return this.getOne(wrapper);
    }

    @Override
    public void deleteByUserId(String userId) {
        var wrapper = new LambdaQueryWrapper<Account>()
                .eq(Account::getUserId, userId);
        this.remove(wrapper);
    }
}

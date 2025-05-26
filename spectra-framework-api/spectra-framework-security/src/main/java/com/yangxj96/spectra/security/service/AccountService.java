package com.yangxj96.spectra.security.service;

import com.yangxj96.spectra.core.base.BaseService;
import com.yangxj96.spectra.security.entity.dto.Account;

public interface AccountService extends BaseService<Account> {

    /**
     * 根据用户名查询账号信息
     *
     * @param username 用户名
     * @return 账号信息
     */
    Account getByUsername(String username);
}

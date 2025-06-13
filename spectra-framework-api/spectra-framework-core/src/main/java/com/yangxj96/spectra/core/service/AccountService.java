package com.yangxj96.spectra.core.service;

import com.yangxj96.spectra.common.base.BaseService;
import com.yangxj96.spectra.core.javabean.entity.Account;

/**
 * 账号service层
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public interface AccountService extends BaseService<Account> {

    /**
     * 根据用户名查询账号信息
     *
     * @param username 用户名
     * @return 账号信息
     */
    Account getByUsername(String username);
}

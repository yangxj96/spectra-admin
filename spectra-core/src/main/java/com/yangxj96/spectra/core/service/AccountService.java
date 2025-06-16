package com.yangxj96.spectra.core.service;

import com.yangxj96.spectra.common.base.BaseService;
import com.yangxj96.spectra.core.javabean.entity.Account;

import java.util.List;

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

    /**
     * <p>根据用户ID获取用户默认账号</p>
     * <p>默认账号只要用户存在就肯定存在</p>
     *
     * @param uid 用户id
     * @return 默认账号
     */
    Account getDefaultAccountByUserId(Long uid);

    /**
     * 根据用户ID删除所有关联的账号信息
     *
     * @param uid 用户ID
     */
    void removeByUserId(Long uid);

    /**
     * 根据用户ID获取关联账号信息
     *
     * @param uid 用户ID
     * @return 账号信息
     */
    List<Account> getByUserId(Long uid);

}

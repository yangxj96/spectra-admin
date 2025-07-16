package com.yangxj96.spectra.share.service;

import com.yangxj96.spectra.share.javabean.ShareAccount;

import java.util.List;

/**
 * 共享账号接口
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
public interface ShareAccountService {

    /**
     * 保存账号
     *
     * @param account 共享账号信息
     * @return 是否保存成功
     */
    boolean save(ShareAccount account);

    /**
     * <p>根据用户ID获取用户默认账号</p>
     * <p>默认账号只要用户存在就肯定存在</p>
     *
     * @param uid 用户id
     * @return 默认账号
     */
    ShareAccount getDefaultAccountByUserId(Long uid);

    /**
     * 根据账号ID修改账号信息
     *
     * @param account 账号信息
     */
    boolean updateById(ShareAccount account);

    /**
     * 根据用户ID获取账号信息列表,因为可能一个用户有多种登录方式
     *
     * @param id 用户ID
     * @return 账号列表
     */
    List<ShareAccount> getByUserId(Long id);

    /**
     * 根据用户ID删除账号列表
     *
     * @param id 用户ID
     */
    void removeByUserId(Long id);
}

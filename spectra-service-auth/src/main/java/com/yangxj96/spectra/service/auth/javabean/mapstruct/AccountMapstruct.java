package com.yangxj96.spectra.service.auth.javabean.mapstruct;

import com.yangxj96.spectra.service.auth.javabean.entity.Account;
import com.yangxj96.spectra.share.javabean.ShareAccount;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 账号信息mapstruct
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
@Mapper(componentModel = "spring")
public interface AccountMapstruct {

    /**
     * 共享账号信息转实体账号信息
     *
     * @param shareAccount 共享账号信息
     * @return 实体账号信息
     */
    Account shareToEntity(ShareAccount shareAccount);


    /**
     * 实体账号信息转共享账号信息
     *
     * @param account 实体账号信息
     * @return 共享账号信息
     */
    ShareAccount entityToShare(Account account);


    /**
     * 实体账号信息列表转共享账号信息列表
     *
     * @param account 实体账号信息列表
     * @return 共享账号信息列表
     */
    List<ShareAccount> entityToShares(List<Account> account);

}

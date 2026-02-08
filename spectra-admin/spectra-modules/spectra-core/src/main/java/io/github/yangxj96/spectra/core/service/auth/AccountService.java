package io.github.yangxj96.spectra.core.service.auth;


import io.github.yangxj96.spectra.common.base.BaseService;
import io.github.yangxj96.spectra.core.javabean.auth.entity.Account;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// 账号服务
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/11 17:05
@NullMarked
public interface AccountService extends BaseService<Account> {

    /**
     * 根据 LoginName 字段查询账号信息
     *
     * @param loginName 登录用户名
     * @return 账号信息，可能为null
     */
    @Nullable Account getByLoginName(String loginName);

    /**
     * 根据用户ID获取用户的默认账号
     *
     * @param userId 用户ID
     * @return 账号信息
     */
    Account getDefaultByUserId(String userId);

    /**
     * 根据用户ID删除用户的所有登录方式
     *
     * @param userId 用户ID
     */
    void deleteByUserId(String userId);
}

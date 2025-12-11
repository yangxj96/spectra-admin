package io.github.yangxj96.spectra.core.service.auth;


import io.github.yangxj96.spectra.common.base.BaseService;
import io.github.yangxj96.spectra.core.javabean.auth.entity.Account;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * 账号服务
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/11 17:05
 */
@NullMarked
public interface AccountService extends BaseService<Account> {

    /**
     * 根据用户 ID 获取登录方式
     *
     * @param userId 用户 ID
     * @return 登录方式,不会为null,因为最少也要又一个账号密码登录方式
     */
    List<Account> getByUserId(Long userId);

    /**
     * 根据 LoginName 字段查询账号信息
     * @param loginName
     * @return
     */
    @Nullable Account getByLoginName(String loginName);
}

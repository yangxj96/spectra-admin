package io.github.yangxj96.spectra.security.base.holder;


import io.github.yangxj96.spectra.security.base.constant.LoginType;
import io.github.yangxj96.spectra.security.base.javabean.entity.SecurityUser;
import io.github.yangxj96.spectra.security.base.javabean.vo.TokenVO;
import io.github.yangxj96.spectra.security.base.javabean.vo.UserOnlineVO;
import org.jspecify.annotations.Nullable;

import java.util.List;

/// Token存储相关
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/11 10:06
public interface SecHolderStrategy {

    /// 获取定义的超管角色
    ///
    /// @return 超管角色
    String administrators();

    /// 创建一个 token 且设置安全上下文
    ///
    /// @param user 用户信息
    /// @return token 信息
    TokenVO createToken(SecurityUser user);

    /// 创建一个 token 且设置安全上下文
    ///
    /// @param user      用户信息
    /// @param loginType 登录方式
    /// @return token 信息
    TokenVO createToken(SecurityUser user, LoginType loginType);

    /// 根据 token 删除 key
    ///
    /// @param token token
    void deleteToken(String token);

    /// 根据用户id删除他的所有登录信息
    ///
    /// @param userId 用户ID
    void deleteByUserId(String userId);

    /// 根据用户id删除指定客户端类型的登录信息
    ///
    /// @param userId     用户ID
    /// @param clientType 客户端类型
    void deleteByUserIdAndClient(String userId, LoginType clientType);

    /// 获取在线用户
    List<UserOnlineVO> listOnlineUsers();

    /// 获取当前用户信息
    ///
    /// @return 当前用户信息
    @Nullable SecurityUser getCurrentUser();

    /// 根据 token 获取用户信息
    ///
    /// @param token token 信息
    /// @return 用户信息
    @Nullable SecurityUser getCurrentUser(String token);

    /// 获取当前用户的 token
    ///
    /// @return token 信息,可能为null
    @Nullable String getCurrentToken();

    /// 获取当前用户 ID
    ///
    /// @return 当前用户 ID, 可能为null
    @Nullable String getCurrentUserId();

}

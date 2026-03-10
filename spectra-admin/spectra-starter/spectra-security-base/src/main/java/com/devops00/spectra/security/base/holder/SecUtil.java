package com.devops00.spectra.security.base.holder;


import com.devops00.spectra.common.exception.SpectraException;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import com.devops00.spectra.security.base.javabean.vo.TokenVO;
import com.devops00.spectra.security.base.javabean.vo.UserOnlineVO;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/// Security 静态工具类
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/10 09:19
@NullMarked
public class SecUtil {

    /// 具体业务持有者
    @Nullable
    private static volatile SecHolderStrategy strategy;


    private SecUtil() {
    }


    public static void setStrategy(SecHolderStrategy s) {
        if (strategy != null) {
            throw new IllegalStateException(
                    "SecHolderStrategy already initialized"
            );
        }
        strategy = s;
    }

    /// 内部调用获取 Holder
    ///
    /// @return {@link SecHolderStrategy} holder,为null会直接报错
    private static SecHolderStrategy getStrategy() {
        SecHolderStrategy s = strategy;
        if (s == null) {
            throw new IllegalStateException(
                    "SecUtil尚未初始化，请确保已加载对应的Security策略"
            );
        }
        return s;
    }

    public static void setHolder(SecHolderStrategy holder) {
        SecUtil.strategy = holder;
    }

    /// 根据用户信息进行登录操作
    ///
    /// @param su 用户信息
    /// @return 登录后的 token 信息
    public static TokenVO login(SecurityUser su) {
        // 默认创建的就是密码,后面添加多个登录方式就要调整下
        return getStrategy().createToken(su);
    }

    /// 根据用户信息登出
    ///
    /// @param token 用户 token
    public static void logout(String token) {
        getStrategy().deleteToken(token);
    }

    /// 登出当前用户
    public static void logout() {
        var token = getStrategy().getCurrentToken();
        if (token == null) {
            throw new SpectraException("无Token/Token无效");
        }
        logout(token);
    }

    /// 根据用户ID踢出用户
    ///
    /// @param id 用户ID
    public static void kick(String id) {
        getStrategy().deleteByUserId(id);
    }

    /// 获取在线用户列表
    public static List<UserOnlineVO> online() {
        return getStrategy().listOnlineUsers();
    }

    /// 根据用户 token 获取用户信息
    ///
    /// @param token token
    /// @return 当前用户信息,可能为null
    public static @Nullable SecurityUser getCurrentUser(String token) {
        return getStrategy().getCurrentUser(token);
    }

    /// 获取当前请求的用户信息
    ///
    /// @return 当前用户信息,可能为null
    public static @Nullable SecurityUser getCurrentUser() {
        return getStrategy().getCurrentUser();
    }

    /// 获取当前用户的 token
    ///
    /// @return 当前用户的 token,可能为null
    public static @Nullable String getCurrentToken() {
        return getStrategy().getCurrentToken();
    }

    /// 获取当前用户 ID
    ///
    /// @return 用户 ID,可能为null
    public static @Nullable String getCurrentUserId() {
        return getStrategy().getCurrentUserId();
    }

    /// 获取当前用户时区ID
    ///
    /// @return 时区ID
    public static String getCurrentUserZoneId() {
        return getStrategy().getCurrentUserZoneId();
    }
}

package io.github.yangxj96.spectra.core.configure.security.holder;


import io.github.yangxj96.spectra.common.exception.SpectraException;
import io.github.yangxj96.spectra.core.configure.security.javabean.SecurityUser;
import io.github.yangxj96.spectra.core.javabean.auth.vo.TokenVO;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/// Security 静态工具类
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/10 09:19
@NullMarked
public class SecUtil {

    /// 具体业务持有者
    @Nullable
    private static SecHolder holder;

    /// 是否初始化
    private static boolean initialized = false;

    private SecUtil() {
    }

    public static void setHolder(SecHolder holder) {
        SecUtil.holder = holder;
        initialized = true;
    }

    /// 内部调用获取 Holder
    ///
    /// @return {@link SecHolder} holder,为null会直接报错
    private static SecHolder getHolder() {
        if (!initialized || holder == null) {
            throw new IllegalStateException("SecUtil 尚未初始化，请确保 SecAutoConfiguration 已加载");
        }
        return holder;
    }

    /// 根据用户信息进行登录操作
    ///
    /// @param su 用户信息
    /// @return 登录后的 token 信息
    public static TokenVO login(SecurityUser su) {
        // 默认创建的就是密码,后面添加多个登录方式就要调整下
        return getHolder().createToken(su);
    }

    /// 根据用户信息登出
    ///
    /// @param token 用户 token
    public static void logout(String token) {
        getHolder().deleteToken(token);
    }

    /// 登出当前用户
    public static void logout() {
        var token = getHolder().getCurrentToken();
        if (token == null) {
            throw new SpectraException("无Token/Token无效");
        }
        logout(token);
    }

    /// 根据用户 token 获取用户信息
    ///
    /// @param token token
    /// @return 当前用户信息,可能为null
    public static @Nullable SecurityUser getCurrentUser(String token) {
        return getHolder().getCurrentUser(token);
    }

    /// 获取当前请求的用户信息
    ///
    /// @return 当前用户信息,可能为null
    public static @Nullable SecurityUser getCurrentUser() {
        return getHolder().getCurrentUser();
    }

    /// 获取当前用户的 token
    ///
    /// @return 当前用户的 token,可能为null
    public static @Nullable String getCurrentToken() {
        return getHolder().getCurrentToken();
    }

    /// 获取当前用户 ID
    ///
    /// @return 用户 ID,可能为null
    public static @Nullable Long getCurrentUserId() {
        return getHolder().getCurrentUserId();
    }

}

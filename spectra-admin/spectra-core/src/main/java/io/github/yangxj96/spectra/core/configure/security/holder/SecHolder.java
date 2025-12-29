package io.github.yangxj96.spectra.core.configure.security.holder;


import io.github.yangxj96.spectra.core.configure.security.javabean.SecurityUser;
import io.github.yangxj96.spectra.core.configure.security.javabean.LoginType;
import io.github.yangxj96.spectra.core.javabean.auth.vo.TokenVO;
import org.jspecify.annotations.Nullable;

/**
 * Token存储相关
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/11 10:06
 */
public interface SecHolder {

    /**
     * 获取定义的超管角色
     *
     * @return 超管角色
     */
    String administrators();

    /**
     * 创建一个 token 且设置安全上下文
     *
     * @param su {@link SecurityUser} 用户信息
     * @return {@link TokenVO} token 信息
     */
    TokenVO createToken(SecurityUser user);

    /**
     * 创建一个 token 且设置安全上下文
     *
     * @param su        {@link SecurityUser} 用户信息
     * @param loginType {@link LoginType} 登录方式
     * @return {@link TokenVO} token 信息
     */
    TokenVO createToken(SecurityUser user, LoginType loginType);

    /**
     * 根据 token 删除 key
     *
     * @param token token
     */
    void deleteToken(String token);

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    @Nullable SecurityUser getCurrentUser();

    /**
     * 根据 token 获取用户信息
     *
     * @param token token 信息
     * @return 用户信息
     */
    @Nullable SecurityUser getCurrentUser(String token);

    /**
     * 获取当前用户的 token
     *
     * @return token 信息,可能为null
     */
    @Nullable String getCurrentToken();

    /**
     * 获取当前用户 ID
     *
     * @return 当前用户 ID, 可能为null
     */
    @Nullable Long getCurrentUserId();

}

package io.github.yangxj96.spectra.core.configure.security.strategy;


import io.github.yangxj96.spectra.core.configure.security.enums.LoginType;
import io.github.yangxj96.spectra.core.configure.security.SecurityUser;
import io.github.yangxj96.spectra.core.javabean.auth.from.LoginFrom;

/**
 * 登录策略
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/2 23:19
 */
public interface LoginStrategy {

    /**
     * 判断当前策略是否支持这个登录方式
     *
     * @param type 登录方式
     * @return 是否支持
     */
    boolean supports(LoginType type);

    /**
     * 登录认证,登录成功必定返回用户信息,否则会抛出异常
     *
     * @param request 登录请求参数
     * @return 用户信息
     */
    SecurityUser authenticate(LoginFrom request);

}

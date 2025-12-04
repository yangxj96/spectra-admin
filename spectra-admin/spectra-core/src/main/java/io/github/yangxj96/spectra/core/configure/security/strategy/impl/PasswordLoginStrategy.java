package io.github.yangxj96.spectra.core.configure.security.strategy.impl;


import io.github.yangxj96.spectra.common.exception.KaptchaNotMatchException;
import io.github.yangxj96.spectra.core.configure.security.enums.LoginType;
import io.github.yangxj96.spectra.core.configure.security.strategy.AbstractLoginStrategy;
import io.github.yangxj96.spectra.core.javabean.auth.SecurityUser;
import io.github.yangxj96.spectra.core.javabean.auth.from.LoginFrom;
import io.github.yangxj96.spectra.core.javabean.user.entity.User;
import io.github.yangxj96.spectra.core.service.common.KaptchaService;
import io.github.yangxj96.spectra.core.service.user.UserService;
import jakarta.annotation.Resource;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 用户名密码登录
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/2 23:19
 */
@Component
public class PasswordLoginStrategy extends AbstractLoginStrategy {

    @Resource
    private KaptchaService kaptchaService;

    @Resource
    private UserService userService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(LoginType type) {
        return type == LoginType.PASSWORD;
    }

    @Override
    @NullMarked
    public SecurityUser authenticate(LoginFrom request) {
        try {
            // 验证码验证
            if (kaptchaService.isCheck() == Boolean.TRUE) {
                var code = kaptchaService.getKaptchaCode();
                if (!request.captcha().equals(code)) {
                    throw new KaptchaNotMatchException("验证码错误");
                }
            }
            // 查询用户信息
            User user = userService.getByEmail(request.identifier());
            if (user == null || !passwordEncoder.matches(request.credential(), user.getPassword())) {
                throw new BadCredentialsException("账号或密码错误");
            }
            // 验证通过,封装返回
            return toSecurityUser(user);
        } finally {
            // 不管登录是否失败,都需要删除掉验证码
            kaptchaService.deleteBySessionId();
        }
    }

}

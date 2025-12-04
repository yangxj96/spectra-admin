package io.github.yangxj96.spectra.core.configure.security.strategy.impl;


import io.github.yangxj96.spectra.common.exception.NotImplementedException;
import io.github.yangxj96.spectra.core.configure.security.enums.LoginType;
import io.github.yangxj96.spectra.core.configure.security.strategy.AbstractLoginStrategy;
import io.github.yangxj96.spectra.core.javabean.auth.SecurityUser;
import io.github.yangxj96.spectra.core.javabean.auth.from.LoginFrom;
import org.springframework.stereotype.Component;

/**
 * 短信登录
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/2 23:19
 */
@Component
public class SmsLoginStrategy extends AbstractLoginStrategy {


    @Override
    public boolean supports(LoginType type) {
        return type == LoginType.SMS;
    }

    @Override
    public SecurityUser authenticate(LoginFrom request) {
        throw new NotImplementedException("短信登录尚未实现");
    }


}

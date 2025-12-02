package io.github.yangxj96.spectra.core.configure.security.strategy.impl;


import io.github.yangxj96.spectra.common.enums.LoginType;
import io.github.yangxj96.spectra.core.configure.security.strategy.AbstractLoginStrategy;
import io.github.yangxj96.spectra.core.configure.security.strategy.LoginStrategy;
import io.github.yangxj96.spectra.core.javabean.auth.javabean.dto.SecurityUser;
import io.github.yangxj96.spectra.core.javabean.auth.javabean.from.LoginFrom;
import org.springframework.stereotype.Component;

/**
 * 二维码登录
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/2 23:19
 */
@Component
public class QrCodeLoginStrategy extends AbstractLoginStrategy {


    @Override
    public boolean supports(LoginType type) {
        return type == LoginType.SCAN;
    }

    @Override
    public SecurityUser authenticate(LoginFrom request) {
        return null;
    }


}

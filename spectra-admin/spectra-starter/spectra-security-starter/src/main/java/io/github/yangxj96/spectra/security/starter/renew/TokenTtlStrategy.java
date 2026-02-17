package io.github.yangxj96.spectra.security.starter.renew;


import io.github.yangxj96.spectra.security.base.constant.LoginType;

public interface TokenTtlStrategy {

    long resolveTtlSeconds(LoginType loginType, String clientType);
}

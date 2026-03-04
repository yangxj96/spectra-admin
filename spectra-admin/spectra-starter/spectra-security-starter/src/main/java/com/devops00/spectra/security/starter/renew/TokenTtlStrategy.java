package com.devops00.spectra.security.starter.renew;


import com.devops00.spectra.security.base.constant.LoginType;

public interface TokenTtlStrategy {

    long resolveTtlSeconds(LoginType loginType, String clientType);
}

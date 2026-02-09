package io.github.yangxj96.spectra.security.starter.renew;

import io.github.yangxj96.spectra.core.configure.security.javabean.LoginType;

public interface TokenTtlStrategy {

    long resolveTtlSeconds(LoginType loginType, String clientType);
}

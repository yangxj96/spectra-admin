package io.github.yangxj96.spectra.core.configure.security.renew;

import io.github.yangxj96.spectra.core.configure.security.javabean.LoginType;

public interface TokenTtlStrategy {

    long resolveTtlSeconds(LoginType loginType, String clientType);
}

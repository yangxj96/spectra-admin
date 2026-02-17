package io.github.yangxj96.spectra.security.starter.renew;

import io.github.yangxj96.spectra.security.base.constant.LoginType;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class DefaultTokenTtlStrategy implements TokenTtlStrategy {

    @Override
    public long resolveTtlSeconds(LoginType loginType, String clientType) {

        if (Objects.equals(clientType, "MOBILE")) {
            return TimeUnit.DAYS.toSeconds(7);
        }

        if (loginType == LoginType.PASSWORD) {
            return TimeUnit.DAYS.toSeconds(30);
        }

        return TimeUnit.HOURS.toSeconds(2);
    }
}

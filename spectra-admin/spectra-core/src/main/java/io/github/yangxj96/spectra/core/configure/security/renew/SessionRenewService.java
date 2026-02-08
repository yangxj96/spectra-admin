package io.github.yangxj96.spectra.core.configure.security.renew;

public interface SessionRenewService {

    /**
     * 尝试对 token 对应的 session 进行续期
     * @return 是否成功续期（可选，用于日志）
     */
    boolean tryRenew(String token);
}

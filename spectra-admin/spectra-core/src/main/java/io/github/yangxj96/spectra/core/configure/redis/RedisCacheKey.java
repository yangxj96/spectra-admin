package io.github.yangxj96.spectra.core.configure.redis;


/**
 * Redis缓存的key
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/4 09:53
 */
public final class RedisCacheKey {

    private RedisCacheKey() {
    }

    /**
     * token存储的格式 <br/>
     * <p>auth:token:{userId}:{tokenId}</p>
     */
    public static final String AUTH_TOKEN_KEY = "authorization:token:%s:%s";

    /**
     * 反向索引:token → userId
     */
    public static final String TOKEN_TO_USER_KEY = "authorization:token-ref:%s";

}

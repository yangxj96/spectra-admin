package io.github.yangxj96.spectra.security.base.constant;


import io.github.yangxj96.spectra.common.constant.RedisKey;

/// 登录需要存储的key的枚举
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/2/8 18:41
public enum AuthRedisKey implements RedisKey {

    /// 登录会话详情（事实源）
    SESSION_TOKEN_DETAIL("auth:session:token:%s"),

    /// 在线用户何几,SESSION级
    SESSION_ONLINE("auth:session:online"),

    /// token -> userId 快速映射
    TOKEN_USER("auth:token:user:%s"),

    /// 用户所有登录 token
    USER_TOKENS("auth:user:tokens:%s"),

    /// 用户在某个客户端的 token 集合
    USER_CLIENT_TOKENS("auth:user:client:tokens:%s:%s"),

    /// 在线用户 ID 集合
    ONLINE_USER_IDS("auth:online:user:ids"),

    /// 用户状态（封禁 / 禁用）
    USER_STATUS("auth:user:status:%s"),

    /// 用户客户端登录限制
    USER_CLIENT_LIMIT("auth:user:client:limit:%s:%s"),

    /// 用户详情(暂时这样存储)
    USER_DETAIL("auth:user:detail:%s");

    private final String pattern;

    AuthRedisKey(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public String getPattern() {
        return pattern;
    }
}

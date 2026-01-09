package io.github.yangxj96.spectra.core.configure.redis;


/// Redis缓存的key
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/4 09:53
public final class RedisCacheKey {

    private RedisCacheKey() {
    }

    /// 当前活跃 Token,存储内容为当前用户的token信息
    /// * 格式:authorization:login_type:{login_type}:{user_id}
    /// * 示例:authorization:login_type:password:1999056543696211969
    public static final String AUTH_LOGIN_TYPE = "authorization:login_type:%s:%s";

    /// token信息,存储token的具体信息(包含扩展信息)
    /// * 格式:authorization:{token}
    /// * 示例:authorization:token:02038A56-527D-498D-8E89-71CCCBAD6B36
    public static final String AUTH_TOKEN = "authorization:token:%s";

    /// security信息,存储security登录的具体信息
    /// * 格式:authorization:security:{token}
    /// * 示例:authorization:security:02038A56-527D-498D-8E89-71CCCBAD6B36
    public static final String AUTH_SECURITY = "authorization:security:%s";

    /// Token 到用户 ID 的快速映射,存储的是用户ID
    /// * 格式:authorization:token_user:{token}
    /// * 示例:authorization:token_user:02038A56-527D-498D-8E89-71CCCBAD6B36
    public static final String AUTH_TOKEN_USER = "authorization:token_user:%s";

    /// 用户所有活跃登录方式集合（用于管理页面）,内容是set
    /// * 格式:authorization:user_login_types:{user_id}
    /// * 示例:authorization:user_login_types:1999056543696211969
    public static final String AUTH_USER_LOGIN_TYPES = "authorization:user_login_types:%s";
}

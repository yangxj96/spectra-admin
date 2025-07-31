package com.yangxj96.spectra.common.constant;

/**
 * Redis常用KEY
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/25
 */
public class RedisKey {

    private RedisKey() {
    }

    /**
     * 验证码
     */
    public static final String KAPTCHA = "kaptcha:";

    /**
     * 用户组织机构列表
     */
    public static final String ORGANIZATION_LIST = "authority:organization:list:";

}

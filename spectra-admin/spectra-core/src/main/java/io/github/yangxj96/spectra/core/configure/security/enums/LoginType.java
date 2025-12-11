package io.github.yangxj96.spectra.core.configure.security.enums;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 登录方式支持
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/2 23:14
 */
public enum LoginType implements IEnum<Integer> {
    /**
     * 账号密码
     */
    PASSWORD(1),
    /**
     * 手机验证码
     */
    SMS(2),
    /**
     * 扫码
     */
    SCAN(3);

    private final Integer value;

    LoginType(Integer value) {
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }
}

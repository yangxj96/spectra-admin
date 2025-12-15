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
    PASSWORD(1,"password"),
    /**
     * 手机验证码
     */
    SMS(2,"sms"),
    /**
     * 扫码
     */
    SCAN(3,"scan");

    private final Integer value;

    private final String name;

    LoginType(Integer value, String name) {
        this.value = value;
        this.name = name;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }

    public String getName(){
        return this.name;
    }
}

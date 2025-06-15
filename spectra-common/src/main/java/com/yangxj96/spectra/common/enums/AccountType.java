package com.yangxj96.spectra.common.enums;


import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * 账号类型
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/15
 */
@Getter
@AllArgsConstructor
public enum AccountType implements IEnum<Short> {
    /**
     * 默认登录,也就是平台账号登录,默认注册的平台登录用邮箱作为登录账号
     */
    DEFAULT((short) 0, "邮箱登录"),
    /**
     * 手机号码登录,预留
     */
    PHONE((short) 1, "手机号码"),
    /**
     * 微信登录,预留
     */
    WECHAT((short) 2, "微信登录");

    private final short value;

    @JsonValue
    private final String desc;

    @Override
    public Short getValue() {
        return this.value;
    }
}
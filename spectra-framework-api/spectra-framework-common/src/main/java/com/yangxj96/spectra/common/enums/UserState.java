package com.yangxj96.spectra.common.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态
 *
 * @author Jack Young
 * @since 2025/6/12 17:03
 */
@Getter
@AllArgsConstructor
public enum UserState implements IEnum<Short> {

    NORMAL((short) 0, "正常"),
    FREEZE((short) 1, "冻结");

    private final short value;

    @JsonValue
    private final String desc;

    @Override
    public Short getValue() {
        return this.value;
    }
}

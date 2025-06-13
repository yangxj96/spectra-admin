package com.yangxj96.spectra.common.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 权限范围
 *
 * @author Jack Young
 * @since 2025/6/12 17:03
 */
@Getter
@AllArgsConstructor
public enum PowerScope implements IEnum<Short> {

    GLOBAL((short) 0, "全局"),
    SELF((short) 1, "本级"),
    SELF_INCLUDE_CHILDREN((short) 2, "本级包含下级");

    private final short value;

    @JsonValue
    private final String desc;

    @Override
    public Short getValue() {
        return this.value;
    }
}

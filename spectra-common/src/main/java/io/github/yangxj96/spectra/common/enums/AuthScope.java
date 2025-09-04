package io.github.yangxj96.spectra.common.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据范围
 */
@Getter
@AllArgsConstructor
public enum AuthScope implements IEnum<Short> {

    ALL((short) 0, "全局"),
    DEPT_AND_CHILD((short) 1, "本级及下级"),
    DEPT_ONLY((short) 2, "本级");

    private final short value;

    @JsonValue
    private final String desc;

    @Override
    public Short getValue() {
        return this.value;
    }

}

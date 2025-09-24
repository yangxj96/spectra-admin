package io.github.yangxj96.spectra.common.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日志类型
 */
@Getter
@AllArgsConstructor
public enum SysLogType implements IEnum<Integer> {

    GENERAL(0, "常规"),

    LOGIN(1, "登录");

    private final Integer value;

    @JsonValue
    private final String desc;

    @Override
    public Integer getValue() {
        return this.value;
    }
}

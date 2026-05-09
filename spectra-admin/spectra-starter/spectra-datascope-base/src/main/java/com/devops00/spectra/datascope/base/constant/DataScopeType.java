package com.devops00.spectra.datascope.base.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/// 数据范围类型
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/2/28 18:00
@Getter
public enum DataScopeType {

    GLOBAL(0, "全局"),

    SELF(1, "本人"),

    DEPT(2, "部门"),

    DEPT_AND_CHILDREN(3, "部门及子部门"),

    CUSTOM(4, "自定义");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    DataScopeType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

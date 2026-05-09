package com.devops00.spectra.datascope.base.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

/// 数据范围类型
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/2/28 18:00
@Getter
public enum DataScopeType {

    GLOBAL(0, "全局"),

    SELF(0, "本人"),

    DEPT(0, "部门"),

    DEPT_AND_CHILDREN(0, "部门及子部门"),

    CUSTOM(0, "自定义");

    @EnumValue
    private final Integer code;

    private final String desc;

    DataScopeType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}

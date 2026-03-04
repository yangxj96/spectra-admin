package com.devops00.spectra.common.constant;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

/// 系统配置值类型
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/25 16:25
@Getter
public enum ConfiguredValueType implements IEnum<Integer> {

    TEXT(0, "文本"),
    BOOL(1, "是否"),
    SELECT(2, "选择");

    /// 值(存数据库用的)
    private final Integer value;

    /// 说明(展示用的)
    private final String name;

    ConfiguredValueType(int type, String name) {
        this.value = type;
        this.name = name;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }

}

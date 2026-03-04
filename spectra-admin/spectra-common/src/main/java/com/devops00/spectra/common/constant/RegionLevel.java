package com.devops00.spectra.common.constant;


import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

/// 行政区划层级
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/1/30 11:20
@Getter
public enum RegionLevel implements IEnum<Integer> {

    /// 省级（省份、直辖市、自治区）
    PROVINCES(1, "省级"),
    /// 地级（城市）
    CITIES(2, "地级"),
    /// 县级（区县）
    AREAS(3, "县级"),
    /// 乡级（乡镇、街道）
    STREETS(4, "乡级"),
    /// 村级（村委会、居委会）
    VILLAGES(5, "村级");

    private final Integer level;

    private final String name;

    RegionLevel(Integer level, String name) {
        this.level = level;
        this.name = name;
    }

    public static RegionLevel of(Integer level) {
        if (level == null) {
            return null;
        }
        for (RegionLevel value : RegionLevel.values()) {
            if (value.level.equals(level)) {
                return value;
            }
        }
        return null;
    }

    @Override
    public Integer getValue() {
        return this.level;
    }

}

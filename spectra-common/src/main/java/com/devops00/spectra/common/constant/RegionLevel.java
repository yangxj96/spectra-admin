/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.common.constant;

import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

/**
 * 行政区划层级
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/1/30 11:20
 */
@Getter
public enum RegionLevel implements IEnum<Integer> {

    /**
     * 省级（省份、直辖市、自治区）
     */
    PROVINCES(1, "省级"),
    /**
     * 地级（城市）
     */
    CITIES(2, "地级"),
    /**
     * 县级（区县）
     */
    AREAS(3, "县级"),
    /**
     * 乡级（乡镇、街道）
     */
    STREETS(4, "乡级"),
    /**
     * 村级（村委会、居委会）
     */
    VILLAGES(5, "村级");

    private final Integer level;

    private final String name;

    RegionLevel(Integer level, String name) {
        this.level = level;
        this.name = name;
    }

    /**
     * 创建或构建目标数据（{@code of}）。
     */
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

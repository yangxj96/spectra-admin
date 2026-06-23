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

/// 系统配置值类型
///
/// @author yangxj96
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

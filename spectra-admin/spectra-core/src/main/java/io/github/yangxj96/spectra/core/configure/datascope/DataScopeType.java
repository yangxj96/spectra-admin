/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.configure.datascope;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/// 数据范围 <br/>
/// 用户直授数据范围 > 角色数据范围 > 默认 SELF
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-11
@Getter
@AllArgsConstructor
public enum DataScopeType implements IEnum<Integer> {

    ALL(0, "全局"),
    SELF(1, "本人"),
    DEPT(2, "部门"),
    DEPT_AND_CHILD(3, "部门及子部门"),
    CUSTOM(4, "自定义");

    /// 存取值
    private final Integer value;

    /// 展示值
    @JsonValue
    private final String desc;

    @Override
    public Integer getValue() {
        return this.value;
    }

}

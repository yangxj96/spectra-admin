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

package com.devops00.spectra.core.system.javabean.enums;

import com.devops00.spectra.common.exception.DataSaveException;

/**
 * 字典项启用状态。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/28
 */
public enum DictItemState {

    /** 可供新业务数据选择。 */
    ENABLED((short) 0),

    /** 保留历史引用，不供新业务数据选择。 */
    DISABLED((short) 1);

    private final short value;

    DictItemState(short value) {
        this.value = value;
    }

    /**
     * 返回字典项状态值。
     *
     * @return 状态值
     */
    public short value() {
        return value;
    }

    /**
     * 根据状态值解析状态。
     *
     * @param value 状态值
     * @return 状态
     */
    public static DictItemState fromValue(Short value) {
        if (value != null) {
            for (var state : values()) {
                if (state.value == value) {
                    return state;
                }
            }
        }
        throw new DataSaveException("字典项状态无效");
    }
}

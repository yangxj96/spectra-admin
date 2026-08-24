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

package com.devops00.spectra.oa.supply.javabean.constant;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 办公用品状态。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
public enum SupplyItemStatus implements IEnum<String> {

    /** 启用。 */
    ACTIVE("ACTIVE"),
    /** 停用。 */
    INACTIVE("INACTIVE");

    private final String value;

    SupplyItemStatus(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}

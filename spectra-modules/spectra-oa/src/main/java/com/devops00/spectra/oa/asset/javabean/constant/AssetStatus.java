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

package com.devops00.spectra.oa.asset.javabean.constant;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 资产状态。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
public enum AssetStatus implements IEnum<String> {

    /** 草稿。 */
    DRAFT("DRAFT"),
    /** 库存中。 */
    IN_STOCK("IN_STOCK"),
    /** 使用中。 */
    IN_USE("IN_USE"),
    /** 维修中。 */
    MAINTENANCE("MAINTENANCE"),
    /** 已报废。 */
    SCRAPPED("SCRAPPED");

    private final String value;

    AssetStatus(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}

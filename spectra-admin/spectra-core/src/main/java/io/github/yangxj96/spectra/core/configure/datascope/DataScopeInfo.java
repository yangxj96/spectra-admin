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

import io.github.yangxj96.spectra.common.enums.AuthScope;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据范围上下文信息
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataScopeInfo {

    /**
     * 是否开启数据过滤
     */
    private Boolean filter;

    /**
     * 数据范围
     */
    private AuthScope scope;

}

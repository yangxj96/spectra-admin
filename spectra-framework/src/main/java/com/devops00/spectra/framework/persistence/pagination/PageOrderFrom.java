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

package com.devops00.spectra.framework.persistence.pagination;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * HTTP 排序项；保留原始字段值，由当前用例的字段白名单验证。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/08
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageOrderFrom {

    /** 端点公布的排序字段名，不接受 SQL 表达式或客户端表别名。 */
    private String column;

    /** 是否升序，未提供时使用升序。 */
    private boolean asc = true;
}

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

package com.devops00.spectra.oa.contract.javabean.from;

import lombok.Data;

/**
 * 合同分页筛选参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/8
 */
@Data
public class ContractPageFrom {

    /**
     * 搜索关键字。
     */
    private String keyword;

    /**
     * 状态。
     */
    private String status;

    /**
     * 合同类型字段。
     */
    private String contractType;

    /**
     * 签署状态。
     */
    private String signingStatus;
}

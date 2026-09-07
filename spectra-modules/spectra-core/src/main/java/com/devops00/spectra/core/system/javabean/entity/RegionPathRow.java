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

package com.devops00.spectra.core.system.javabean.entity;

import lombok.Data;

import java.util.UUID;

/**
 * 行政区域递归路径查询行。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/7
 */
@Data
public class RegionPathRow {

    /** 区域 ID。 */
    private UUID id;

    /** 上级区域 ID。 */
    private UUID pid;

    /** 区域名称。 */
    private String name;

    /** 从目标节点向上计算的递归深度。 */
    private int depth;
}

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

package com.devops00.spectra.core.system.javabean.vo;

import lombok.Builder;
import lombok.Data;

/// CPU信息响应实体
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/11 00:00
@Data
@Builder
public class CPUInfoVO {

    /// CPU名称
    private String name;

    /// CPU 制造商
    private String vendor;

    /// 家族编号
    private String family;

    /// 型号编号
    private String model;

    /// 步进(修订版本)
    private String stepping;

    /// 完整标识字符串
    private String identifier;

    /// 是否64位
    private Boolean is64bit;

    /// 物理核心数量
    private Integer physicalCores;

    /// 逻辑核心数（支持超线程）
    private Integer logicalCores;

    /// 最大支持频率
    private Long maxFrequencyHz;

    /// 最大支持频率
    private String maxFrequencyGhz;
}

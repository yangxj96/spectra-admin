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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/// 内存信息
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/11 00:00
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RAMInfoVO {

    /// 摘要。
    private String summary;

    /// 数量。
    private String count;

    /// 总容量（字节）。
    private Long totalCapacityBytes;

    /// 总容量（GB）。
    private String totalCapacityGB;

    /// 内存插槽列表。
    private List<RAMSlot> slots;

    /// 内存插槽信息。
    @Data
    @Builder
    public static class RAMSlot {

        /// 插槽编号。
        private Integer slot;

        /// 内存类型。
        private String memoryType;

        /// 时钟频率，单位 Hz。
        private Long clockSpeedHz;

        /// 时钟频率，单位 MHz。
        private String clockSpeedMHz;

        /// 容量，单位字节。
        private Long capacityBytes;

        /// 容量，单位 GB。
        private String capacityGB;

    }

}

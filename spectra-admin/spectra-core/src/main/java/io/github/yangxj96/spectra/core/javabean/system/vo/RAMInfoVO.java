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

package io.github.yangxj96.spectra.core.javabean.system.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 内存信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RAMInfoVO {

    private String summary;
    private String count;
    private Long totalCapacityBytes;
    private String totalCapacityGB;

    private List<RAMSlot> slots;

    @Data
    @Builder
    public static class RAMSlot {

        private Integer slot;

        private String memoryType;

        private Long clockSpeedHz;

        private String clockSpeedMHz;

        private Long capacityBytes;

        private String capacityGB;

    }

}

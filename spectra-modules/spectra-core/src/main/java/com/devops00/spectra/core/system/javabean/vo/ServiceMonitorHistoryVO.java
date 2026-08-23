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

import java.time.LocalDateTime;
import java.util.List;

/** 服务监控历史趋势。 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMonitorHistoryVO {

    /** 查询范围。 */
    private String range;

    /** 查询起始时间。 */
    private LocalDateTime from;

    /** 查询结束时间。 */
    private LocalDateTime to;

    /** 趋势点。 */
    @Builder.Default
    private List<ServiceMonitorOverviewVO.Point> points = List.of();
}

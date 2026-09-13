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

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 服务监控告警规则视图。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Data
@Builder
public class ServiceMonitorAlertRuleVO {

    private UUID id;
    private String code;
    private String name;
    private String metricCode;
    private String metricLabel;
    private String operatorCode;
    private Double thresholdValue;
    private String expectedValue;
    private String severity;
    private Boolean enabled;
    private Integer consecutiveFailures;
    private Integer cooldownSeconds;
    private String remark;
    private Long version;
    private LocalDateTime updatedAt;
}

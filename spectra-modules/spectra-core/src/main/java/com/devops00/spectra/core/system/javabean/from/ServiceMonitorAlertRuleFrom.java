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

package com.devops00.spectra.core.system.javabean.from;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * 服务监控告警规则修改请求。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Data
public class ServiceMonitorAlertRuleFrom {

    private UUID id;

    @NotNull(message = "规则版本不能为空")
    private Long expectedVersion;

    private String name;
    private String operatorCode;
    private Double thresholdValue;
    private String expectedValue;
    private String severity;
    private Boolean enabled;
    private Integer consecutiveFailures;
    private Integer cooldownSeconds;
    private String remark;
}

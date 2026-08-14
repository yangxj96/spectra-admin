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

package com.devops00.spectra.core.security.policy.javabean.from;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 会话策略修改入参。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecuritySessionPolicyFrom {

    @NotBlank(message = "会话并发模式不能为空")
    private String concurrencyMode;

    @NotNull(message = "是否允许并发不能为空")
    private Boolean allowConcurrent;

    @NotNull(message = "最大会话数不能为空")
    @Positive(message = "最大会话数必须为正数")
    private Integer maxSessions;

    @NotNull(message = "Access TTL 不能为空")
    @Positive(message = "Access TTL 必须为正数")
    private Integer accessTtlSeconds;

    @NotNull(message = "Refresh TTL 不能为空")
    @Positive(message = "Refresh TTL 必须为正数")
    private Integer refreshTtlSeconds;

    @Positive(message = "绝对 TTL 必须为正数")
    private Integer absoluteTtlSeconds;

    @Positive(message = "空闲 TTL 必须为正数")
    private Integer idleTtlSeconds;

    @NotNull(message = "策略版本不能为空")
    @PositiveOrZero(message = "策略版本不能为负数")
    private Long expectedVersion;
}

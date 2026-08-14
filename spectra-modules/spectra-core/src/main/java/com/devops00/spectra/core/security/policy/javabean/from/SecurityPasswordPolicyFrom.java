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

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 系统密码策略修改入参。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityPasswordPolicyFrom {

    @NotNull(message = "密码最小长度不能为空")
    @Min(value = 8, message = "密码最小长度不能低于 8")
    private Integer minLength;

    @NotNull(message = "大写字母策略不能为空")
    private Boolean requireUppercase;

    @NotNull(message = "小写字母策略不能为空")
    private Boolean requireLowercase;

    @NotNull(message = "数字策略不能为空")
    private Boolean requireDigit;

    @NotNull(message = "特殊字符策略不能为空")
    private Boolean requireSpecial;

    @Positive(message = "密码最大有效天数必须为正数")
    private Integer maxAgeDays;

    @NotNull(message = "策略版本不能为空")
    @PositiveOrZero(message = "策略版本不能为负数")
    private Long expectedVersion;
}

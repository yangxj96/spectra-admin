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

package com.devops00.spectra.core.system.javabean.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 服务监控告警比较操作符。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
public enum ServiceMonitorAlertOperator {

    /** 大于等于。 */
    GTE,
    /** 大于。 */
    GT,
    /** 小于等于。 */
    LTE,
    /** 小于。 */
    LT,
    /** 等于。 */
    EQ,
    /** 不等于。 */
    NE;

    private static final Set<String> CODES = Arrays.stream(values()).map(Enum::name).collect(Collectors.toUnmodifiableSet());

    /**
     * 判断编码是否为受支持的操作符。
     *
     * @param code 操作符编码
     * @return 是否受支持
     */
    public static boolean contains(String code) {
        return code != null && CODES.contains(code);
    }
}

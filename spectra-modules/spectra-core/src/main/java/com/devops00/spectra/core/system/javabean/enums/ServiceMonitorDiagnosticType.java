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

import lombok.Getter;

/**
 * 服务监控诊断任务类型。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Getter
public enum ServiceMonitorDiagnosticType {

    THREAD_DUMP("THREAD_DUMP", "线程转储", ".txt"),
    HEAP_DUMP("HEAP_DUMP", "堆转储", ".hprof");

    private final String code;
    private final String label;
    private final String suffix;

    ServiceMonitorDiagnosticType(String code, String label, String suffix) {
        this.code = code;
        this.label = label;
        this.suffix = suffix;
    }

    /**
     * 转换、解析或规范化数据（{@code fromCode}）。
     */
    public static ServiceMonitorDiagnosticType fromCode(String code) {
        for (var type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}

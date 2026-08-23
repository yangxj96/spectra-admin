/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.enums;

import lombok.Getter;

/** 服务监控诊断任务类型。 */
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

    public static ServiceMonitorDiagnosticType fromCode(String code) {
        for (var type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}

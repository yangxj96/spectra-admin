package io.github.yangxj96.spectra.common.constant;

import org.jspecify.annotations.NullMarked;

/// 统一日志前缀定义
///
/// 用于规范不同模块日志输出格式
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/2/17 23:14
@NullMarked
public enum LogPrefix {

    CORE("CORE"),
    SECURITY("SECURITY"),
    AUTH("AUTH"),
    WEB("WEB"),
    DB("DB");

    private final String value;

    LogPrefix(String value) {
        this.value = value;
    }

    /**
     * 获取带中括号的标准前缀
     */
    public String p() {
        return "[" + value + "] ";
    }

    /**
     * 拼接日志内容
     */
    public String f(String message) {
        return p() + message;
    }

}

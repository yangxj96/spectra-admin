package com.devops00.spectra.common.constant;

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

    CORE("核心"),
    SECURITY("安全"),
    STORAGE("文件存储"),
    SERIALIZATION("序列化"),
    KAPTCHA("验证码"),
    WEB("WEB"),
    PERSISTENCE("持久化"),
    REDIS("Redis"),
    CACHE("缓存"),
    LOG("日志");

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

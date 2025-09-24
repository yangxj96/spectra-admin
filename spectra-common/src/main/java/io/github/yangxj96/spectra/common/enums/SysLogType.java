package io.github.yangxj96.spectra.common.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 日志类型
 */
@Getter
@AllArgsConstructor
public enum SysLogType implements IEnum<Integer> {

    /**
     * 常规日志,主要是接口调用相关
     */
    GENERAL(0, "常规日志"),

    /**
     * 安全日志,账号登录,登出,改密码,封号等
     */
    SAFETY(1, "安全日志"),

    /**
     * 系统出现异常的时候进行记录
     */
    SYSTEM_ERROR(2, "系统异常日志"),

    /**
     * 定时任务等自动化操作的日志
     */
    AUTOMATE(3, "自动化日志");

    private final Integer value;

    @JsonValue
    private final String desc;

    @Override
    public Integer getValue() {
        return this.value;
    }
}

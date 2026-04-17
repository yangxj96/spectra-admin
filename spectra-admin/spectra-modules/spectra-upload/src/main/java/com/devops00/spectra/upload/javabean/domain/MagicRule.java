package com.devops00.spectra.upload.javabean.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/// 文件魔数规则
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/17 9:42
@Getter
@Setter
@ToString
public class MagicRule {

    /**
     * 魔数（十六进制字符串）
     * 示例: FFD8FF
     */
    private String bytes;

    /**
     * 偏移量
     */
    private Integer offset = 0;

    /**
     * 可选：描述
     */
    private String description;

    /**
     * 编译后的字节（不入库）
     */
    private transient byte[] compiled;
}
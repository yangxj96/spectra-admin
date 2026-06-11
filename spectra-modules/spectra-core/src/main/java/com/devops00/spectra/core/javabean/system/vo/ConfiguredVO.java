package com.devops00.spectra.core.javabean.system.vo;

import com.devops00.spectra.common.constant.ConfiguredValueType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/// 系统配置分页响应
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-11
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfiguredVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /// 主键ID
    private UUID id;

    /// 配置key
    private String key;

    /// 配置VALUE
    private String value;

    /// 值类型
    private ConfiguredValueType type;

    /// 字典code
    private String dictCode;

    /// 备注说明
    private String remarks;

}

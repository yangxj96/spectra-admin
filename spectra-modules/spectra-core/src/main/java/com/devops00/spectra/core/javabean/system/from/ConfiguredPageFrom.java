package com.devops00.spectra.core.javabean.system.from;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/// 系统配置分页查询入参
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-06
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfiguredPageFrom {

    /// 系统配置的key,模糊查询
    private String key;

}

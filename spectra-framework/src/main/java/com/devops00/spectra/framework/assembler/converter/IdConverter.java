package com.devops00.spectra.framework.assembler.converter;

/// ID转换器
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/20 11:41
public interface IdConverter<ID> {

    /// 转换到字符串
    ///
    /// @param id 值
    String toString(ID id);

    /// 转换到指定类型
    ///
    /// @param value 值
    ID fromString(String value);
}
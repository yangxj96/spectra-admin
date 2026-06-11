package com.devops00.spectra.framework.assembler.converter;

/// Long作为主键的转换器
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/20 11:41
public class LongConverter implements IdConverter<Long> {

    @Override
    public String toString(Long id) {
        return id == null ? null : id.toString();
    }

    @Override
    public Long fromString(String value) {
        return value == null ? null : Long.valueOf(value);
    }
}